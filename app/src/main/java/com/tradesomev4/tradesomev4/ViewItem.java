package com.tradesomev4.tradesomev4;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tradesomev4.tradesomev4.m_Helpers.CalendarUtils;
import com.tradesomev4.tradesomev4.m_Helpers.DistanceHelper;
import com.tradesomev4.tradesomev4.m_Helpers.IsBlockedListener;
import com.tradesomev4.tradesomev4.m_Helpers.ItemImageSwipe;
import com.tradesomev4.tradesomev4.m_Model.Auction;
import com.tradesomev4.tradesomev4.m_Model.User;
import com.tradesomev4.tradesomev4.m_UI.ParticipantAdapter;


/**
 * Created by Jorge Benigno Pante, Charles Torrente, Joshua Alarcon on 7/17/2016.
 * File name: ViewItem.java
 * File Path: Tradesomev4\app\src\main\java\com\tradesomev4\tradesomev4\ViewItem.java
 * Description: View item posted by other user full details.
 */

public class ViewItem extends AppCompatActivity implements View.OnClickListener {
    private static final String DEBUG_TAG = "DEBUG_TAG";
    private static final String EXTRAS_AUCTION_ID = "AUCTION_ID";
    private static final String EXTRAS_POSTER_ID = "POSTER_ID";
    private static final String EXTRAS_BUNDLE = "EXTRAS_BUNDLE";
    private String auctionId;
    private String posterId;
    private DatabaseReference mDatabase;
    private ImageView posterImage;
    private TextView posterName;
    private ViewPager swipeItem;
    private ImageView bidNow;
    private ImageView itemLocation;
    private ImageView reportItem;
    private TextView itemTitle;
    private TextView distance;
    private TextView startingBid;
    private TextView description;
    private ItemImageSwipe itemImageSwipe;
    private double userLat;
    private double userLong;
    private double posterLat;
    private double posterLong;
    private FirebaseUser fUser;
    private Button bidNowBtn;
    private TextView datePost;
    private TextView bids;
    Bundle extras;
    private SlidingUpPanelLayout mLayout;
    private RecyclerView recyclerView;
    private ParticipantAdapter adapter;
    private SearchView sv;
    TextView tv_items_here;
    TextView tv_internet_connection;
    ProgressWheel progress_wheel;
    View content_main;
    View view;

    public void initSlidingUp() {
        boolean isAttached;
        onAttachedToWindow();
        isAttached = true;
        content_main = findViewById(R.id.content_main);
        recyclerView = (RecyclerView) findViewById(R.id.rv_participants);
        adapter = new ParticipantAdapter(this, extras.getString(EXTRAS_AUCTION_ID), extras.getString(EXTRAS_POSTER_ID), isAttached, recyclerView, Glide.with(this), tv_items_here, tv_internet_connection, progress_wheel, content_main);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(DEBUG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(DEBUG_TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        mLayout.setAnchorPoint(0.7f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);
        try {
            extras = getIntent().getBundleExtra(EXTRAS_BUNDLE);

            Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        content_main = findViewById(R.id.sliding_layout);
        tv_items_here = (TextView) findViewById(R.id.tv_items_here);
        tv_internet_connection = (TextView) findViewById(R.id.tv_internet_connection);
        progress_wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        tv_items_here.setVisibility(View.GONE);
        tv_internet_connection.setVisibility(View.GONE);

        initSlidingUp();

        posterImage = (ImageView) findViewById(R.id.iv_poster_image);
        posterName = (TextView) findViewById(R.id.tv_poster_name);
        swipeItem = (ViewPager) findViewById(R.id.vp_swipe_item_image);
        itemLocation = (ImageView) findViewById(R.id.iv_item_location);
        bidNow = (ImageView) findViewById(R.id.iv_bid_now);
        bidNowBtn = (Button) findViewById(R.id.btn_bid_now);
        itemLocation = (ImageView) findViewById(R.id.iv_item_location);
        reportItem = (ImageView) findViewById(R.id.iv_report_item);
        itemTitle = (TextView) findViewById(R.id.tv_item_title);
        distance = (TextView) findViewById(R.id.tv_distance);
        startingBid = (TextView) findViewById(R.id.tv_starting_bid);
        description = (TextView) findViewById(R.id.tv_description);
        datePost = (TextView) findViewById(R.id.tv_date_post);
        bids = (TextView) findViewById(R.id.tv_bids);
        view = findViewById(R.id.view_profile);

        posterImage.setOnClickListener(this);
        posterName.setOnClickListener(this);
        itemLocation.setOnClickListener(this);
        bidNow.setOnClickListener(this);
        reportItem.setOnClickListener(this);
        bidNowBtn.setOnClickListener(this);
        view.setOnClickListener(this);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        try {
            auctionId = extras.getString(EXTRAS_AUCTION_ID);
            posterId = extras.getString(EXTRAS_POSTER_ID);

            new IsBlockedListener(this, true, posterId);
            Query userRef = mDatabase.child("users").child(posterId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    posterLat = user.getLatitude();
                    posterLong = user.getLongitude();
                    Glide.with(getApplicationContext())
                            .load(user.getImage())
                            .asBitmap().centerCrop()
                            .into(posterImage);
                    posterName.setText(user.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mDatabase.child("auction").child(auctionId).child("bid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    bids.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDatabase.child("auction").child(auctionId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("auctionIDDD", auctionId);
                    Auction auction = dataSnapshot.getValue(Auction.class);

                    if(auction == null){
                        itemNotExists();
                    }

                    itemImageSwipe = new ItemImageSwipe(getApplicationContext(),
                            auction.getImage1Uri(),
                            auction.getImage2Uri(),
                            auction.getImage3Uri(),
                            auction.getImage4Uri());
                    swipeItem.setAdapter(itemImageSwipe);

                    itemTitle.setText(auction.getItemTitle());

                    String startidBidAmount = String.format("%d", auction.getStaringBid());
                    startingBid.setText(" Php" + startidBidAmount);
                    description.append(auction.getDescription());
                    datePost.setText(CalendarUtils.ConvertMilliSecondsToFormattedDate(auction.getPostDate() + ""));

                    Query curUser = mDatabase.child("users").child(fUser.getUid());
                    curUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            userLat = user.getLatitude();
                            userLong = user.getLongitude();

                            LatLng user1 = new LatLng(userLat, userLong);
                            LatLng user2 = new LatLng(posterLat, posterLong);
                            Double distanceVal = DistanceHelper.getDistance(user1, user2);

                            distance.setText(DistanceHelper.formatNumber(distanceVal));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void itemNotExists(){
        new MaterialDialog.Builder(this)
                .title("Sorry")
                .content("This item no longer exists")
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .positiveText("BACK")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(which.toString().equals("POSITIVE")){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }).show();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.iv_bid_now:
                    Intent intent = new Intent(getApplicationContext(), BidNow.class);
                    intent.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(intent);
                    break;
                case R.id.iv_item_location:
                    Intent viewLocation = new Intent(getApplicationContext(), ViewItemLocation.class);
                    viewLocation.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(viewLocation);
                    break;
                case R.id.view_profile:
                    Intent viewUserProfile = new Intent(getApplicationContext(), ViewUserProfile.class);
                    viewUserProfile.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(viewUserProfile);
                    break;
                case R.id.tv_poster_name:
                    Intent yeah = new Intent(getApplicationContext(), ViewUserProfile.class);
                    yeah.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(yeah);
                    break;
                case R.id.btn_bid_now:
                    Intent bidNow = new Intent(getApplicationContext(), BidNow.class);
                    bidNow.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(bidNow);
                    break;
                case R.id.iv_report_item:
                    Intent fileAReport = new Intent(getApplicationContext(), FileItemComplain.class);
                    fileAReport.putExtra(EXTRAS_BUNDLE, extras);
                    startActivity(fileAReport);
                    break;

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigateUp() {

        NavUtils.navigateUpFromSameTask(ViewItem.this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
