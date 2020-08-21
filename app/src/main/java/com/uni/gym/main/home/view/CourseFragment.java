package com.uni.gym.main.home.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.share.Share;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.ActivityCheckoutBinding;
import com.uni.gym.databinding.FragmentCourseBinding;
import com.uni.gym.googlepay.PaymentsUtil;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.blog.adapter.CommentAdapter;
import com.uni.gym.main.blog.model.Comment;
import com.uni.gym.main.home.model.Course;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Optional;

public class CourseFragment extends Fragment {

    private FragmentCourseBinding binding;
    private String courseId;
    private DocumentReference documentReference;
    private FirebaseFirestore db;
    private CommentAdapter adapter;
    private Comment comment;
    private OnFragmentInteractionListener mListener;

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final long SHIPPING_COST_CENTS = 90 * PaymentsUtil.CENTS_IN_A_UNIT.longValue();
    private PaymentsClient paymentsClient;
    private View googlePayButton;


    public static CourseFragment newInstance() {
        return new CourseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.commentUserImg.setVisibility(View.INVISIBLE);
        binding.sendComment.setVisibility(View.INVISIBLE);
        binding.userComment.setVisibility(View.INVISIBLE);

        initializeUi();

        paymentsClient = PaymentsUtil.createPaymentsClient(getActivity());
        possiblyShowGooglePayButton();

        db = FirebaseFirestore.getInstance();
        comment = new Comment();

        if (SharedPrefHelper.getUserName(getContext()) != null) {
            comment.setUsername(SharedPrefHelper.getUserName(getContext()));
        }
        if (SharedPrefHelper.getUserPic(getContext()) != null) {
            Glide.with(binding.commentUserImg).load(SharedPrefHelper.getUserPic(getContext())).into(binding.commentUserImg);
            comment.setUserPic(SharedPrefHelper.getUserPic(getContext()));
        }

        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.sendComment.setVisibility(View.GONE);

        if (getArguments().getString("course_id") != null) {
            courseId = getArguments().getString("course_id");
            getCourse();
            setUpRecyclerView();
            binding.sendComment.setOnClickListener(view -> {
                publish();
            });
            binding.userComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().isEmpty())
                        binding.sendComment.setVisibility(View.VISIBLE);
                    else
                        binding.sendComment.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            binding.ratingBar.setIsIndicator(true);
        }

    }

    private void getCourse() {
        documentReference = db.collection("Courses").document(courseId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    Course course = task.getResult().toObject(Course.class);
                    updateCourse(course);
                }
            }
        });
    }

    private void updateCourse(Course course) {
        binding.tvNameCourse.setText(course.getCourseName());
        binding.tvNameCourse.setTextColor(getResources().getColor(R.color.black));
        binding.tvNameCourse.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        String price = course.getPrice() + " US$";
        binding.tvPriceCourse.setText(price);
        binding.tvPriceCourse.setTextColor(getResources().getColor(R.color.black));
        binding.tvPriceCourse.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tvOverview.setText(course.getOverview());
        binding.tvOverview.setTextColor(getResources().getColor(R.color.card_view_courses_txt2));
        binding.tvOverview.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        documentReference.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()) {
                Course myCourse = documentSnapshot.toObject(Course.class);
                binding.ratingBar.setRating(myCourse.getRate());
            }
        });
        setUpRating(course);

    }

    private void setUpRating(Course course) {
        if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member)))
            documentReference.collection("Users").document(SharedPrefHelper.getUserId(getContext())).get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    documentReference.collection("Ratings").document(SharedPrefHelper.getUserId(getContext())).addSnapshotListener((taskRatings, er) -> {
                        if (taskRatings.exists()) {
                            binding.ratingBar.setIsIndicator(false);
                            binding.ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                                documentReference.addSnapshotListener((documentSnapshot, e) -> {
                                    if (documentSnapshot.exists()) {
                                        Course myCourse = documentSnapshot.toObject(Course.class);
                                        binding.ratingBar.setRating(myCourse.getRate());
                                    }
                                });
                                Toast.makeText(getContext(), "you have already rated", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            binding.ratingBar.setIsIndicator(false);
                            binding.ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                                if (b) {
                                    WriteBatch batch = db.batch();
                                    batch.update(documentReference, "rateCount", course.getRateCount() + 1);
                                    float myRating = (v + (course.getRate() * course.getRateCount())) / (course.getRateCount() + 1);
                                    batch.update(documentReference, "rate", myRating);
                                    batch.commit().addOnCompleteListener(task1 -> {
                                        HashMap rateUser = new HashMap<String, String>();
                                        rateUser.put("userId", SharedPrefHelper.getUserId(getContext()));
                                        documentReference.collection("Ratings").document(SharedPrefHelper.getUserId(getContext())).set(rateUser);
                                        Toast.makeText(getContext(), "thanks for rating", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        }
                    });
                } else {
                    binding.ratingBar.setIsIndicator(false);
                    binding.ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                        documentReference.addSnapshotListener((documentSnapshot, e) -> {
                            if (documentSnapshot.exists()) {
                                Course myCourse = documentSnapshot.toObject(Course.class);
                                binding.ratingBar.setRating(myCourse.getRate());
                            }
                        });
                        Toast.makeText(getContext(), "you need to buy the course in order to rate it", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        else {
            binding.ratingBar.setIsIndicator(true);
        }
    }

    private void setUpRecyclerView() {
        Query query = documentReference.collection("Comments").orderBy("time", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Comment> options =
                new FirestoreRecyclerOptions.Builder<Comment>().setLifecycleOwner(this).setQuery(query, Comment.class).build();

        adapter = new CommentAdapter(options);
        binding.rvComments.setHasFixedSize(true);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(adapter);

        documentReference.collection("Comments").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty()) {
                binding.noComments.setVisibility(View.VISIBLE);
            } else {
                binding.noComments.setVisibility(View.INVISIBLE);
            }
        });

        adapter.setOnItemClickListener((position) -> {
            adapter.likeItem(position);
        });
    }

    private void publish() {
        binding.sendComment.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.userComment.getWindowToken(), 0);

        comment.setTime(System.currentTimeMillis());
        comment.setComment(binding.userComment.getText().toString());
        binding.userComment.setText("");

        documentReference.collection("Comments").document().set(comment).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.INVISIBLE);
        });

    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    // Google button
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;

                    case Activity.RESULT_CANCELED:
//                        HashMap courseIds = new HashMap<String, String>();
//                        courseIds.put("course", courseId);
//                        db.collection("GymUsers").document(SharedPrefHelper.getUserId(getContext())).collection("Courses").document(courseId)
//                                .set(courseIds).addOnCompleteListener(task -> {
//                            if (task.isSuccessful())
//                                Toast.makeText(getContext(), "thanks for buying the course", Toast.LENGTH_SHORT).show();
//                            else
//                                Toast.makeText(getContext(), "sorry, something went wrong", Toast.LENGTH_SHORT).show();
//                        });
//                        HashMap userIds = new HashMap<String, String>();
//                        userIds.put("userId", SharedPrefHelper.getUserId(getContext()));
//                        db.collection("Courses").document(courseId).collection("Users").document(SharedPrefHelper.getUserId(getContext())).set(userIds);

                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                }
                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
        }
    }

    private void initializeUi() {

        // The Google Pay button is a layout file – take the root view
        googlePayButton = binding.googlePayButton.getRoot();
        googlePayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPayment();
                    }
                });
    }

    private void possiblyShowGooglePayButton() {

        final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(getActivity(),
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            setGooglePayAvailable(task.getResult());
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }

    private void setGooglePayAvailable(boolean available) {
        if (available) {
            if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member)))
                showGoogleBtn();
            else {
                doTheThing();
            }
        } else {
            Toast.makeText(getContext(), R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void doTheThing() {
        binding.commentUserImg.setVisibility(View.VISIBLE);
        binding.sendComment.setVisibility(View.VISIBLE);
        binding.userComment.setVisibility(View.VISIBLE);
        binding.tvSeeSessions.setOnClickListener(view -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString("course_id", courseId);
            mListener.onFragmentInteraction(4, bundle1);
        });

    }

    private void showGoogleBtn() {
        documentReference.collection("Users").document(SharedPrefHelper.getUserId(getContext())).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    doTheThing();
                } else {
                    googlePayButton.setVisibility(View.VISIBLE);
                    binding.tvSeeSessions.setOnClickListener(view -> {
                        Toast.makeText(getContext(), "you need to buy the course in order to see the sessions", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void handlePaymentSuccess(PaymentData paymentData) {

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        final String paymentInfo = paymentData.toJson();
        if (paymentInfo == null) {
            return;
        }

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".

            final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            final String tokenizationType = tokenizationData.getString("type");
            final String token = tokenizationData.getString("token");

            if ("PAYMENT_GATEWAY".equals(tokenizationType) && "examplePaymentMethodToken".equals(token)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Warning")
                        .setMessage(getString(R.string.gateway_replace_name_example))
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
            }

            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
            Toast.makeText(
                    getContext(), getString(R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show();

            // Logging token string.
            Log.d("Google Pay token: ", token);

        } catch (JSONException e) {
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }

    private void handleError(int statusCode) {
        Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    public void requestPayment() {

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        double garmentPrice = 14.75;
        long garmentPriceCents = Math.round(garmentPrice * PaymentsUtil.CENTS_IN_A_UNIT.longValue());
        long priceCents = garmentPriceCents + SHIPPING_COST_CENTS;

        Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
        if (!paymentDataRequestJson.isPresent()) {
            return;
        }

        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

        // Since may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
        }

    }

}