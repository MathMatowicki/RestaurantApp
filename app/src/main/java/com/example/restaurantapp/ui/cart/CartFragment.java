package com.example.restaurantapp.ui.cart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantapp.Adapter.MyCartAdapter;
import com.example.restaurantapp.Common.Common;
import com.example.restaurantapp.Common.MySwipeHelper;
import com.example.restaurantapp.Database.CartDataSource;
import com.example.restaurantapp.Database.CartDatabase;
import com.example.restaurantapp.Database.CartItem;
import com.example.restaurantapp.Database.LocalCartDataSource;
import com.example.restaurantapp.EventBus.CounterCartEvent;
import com.example.restaurantapp.EventBus.HideFABCart;
import com.example.restaurantapp.EventBus.UpdateItemInCart;
import com.example.restaurantapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @SuppressLint("MissingPermission")
    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton) view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton) view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton) view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton) view.findViewById(R.id.rdi_cod);

//        Data
        edt_address.setText(Common.currentUser.getAddress());

//        Event
        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edt_address.setText(""); // Clear
                edt_address.setHint("Enter Other Address");
                txt_address.setVisibility(View.GONE);
            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failure Location :c", Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        })
                        .addOnCompleteListener(task -> {
                            String coordinates = new StringBuilder()
                                    .append(task.getResult().getLatitude())
                                    .append(task.getResult().getLongitude()).toString();
                            edt_address.setText(coordinates);
                            txt_address.setText("Implement late with google API ");
                            txt_address.setVisibility(View.VISIBLE);
                        });

            }
        });

        builder.setView(view);
        builder.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("YES", (dialog, which) -> {
            Toast.makeText(getContext(), "Implement later!", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private MyCartAdapter myCartAdapter;

    private Unbinder unbinder;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if (cartItems == null || cartItems.isEmpty()) {
                    recycler_cart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);
                } else {
                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    myCartAdapter = new MyCartAdapter(getContext(), cartItems);
                    recycler_cart.setAdapter(myCartAdapter);
                }
            }
        });
        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {
        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buff) {
                buff.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = myCartAdapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            myCartAdapter.notifyItemRemoved(pos);
                                            sumAllItemInCart();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));//Update FAB
                                            Toast.makeText(getContext(), "Delete item from cart success", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            Toast.makeText(getContext(), "Cart Error :c", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));
            }
        };
        sumAllItemInCart();
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: $").append(aDouble));
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "Cart Empty :c", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); // Hide home menu
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "Cart error :c", Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        calculateTotalPrice();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            //save state of RecyclerView
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            calculateTotalPrice();
                            // Fix error refresh recycler view after update
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double price) {
                        txt_total_price.setText(new StringBuilder("Total: $")
                                .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getContext(), "[SUM CART]", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
