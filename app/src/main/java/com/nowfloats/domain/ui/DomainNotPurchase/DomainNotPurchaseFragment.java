package com.nowfloats.domain.ui.DomainNotPurchase;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boost.upgrades.UpgradeActivity;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.HomeActivity;
import com.nowfloats.domain.DomainEmailActivity;
import com.thinksity.R;

import static com.nowfloats.NavigationDrawer.HomeActivity.headerText;

public class DomainNotPurchaseFragment extends Fragment {

    private DomainNotPurchaseViewModel mViewModel;
    private Toolbar toolbar;
    private TextView headerText, buyItem;
    private UserSessionManager session;

    public static DomainNotPurchaseFragment newInstance() {
        return new DomainNotPurchaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        session = new UserSessionManager(requireContext(), requireActivity());
        mViewModel = ViewModelProviders.of(this).get(DomainNotPurchaseViewModel.class);

        return inflater.inflate(R.layout.domain_not_purchase_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buyItem = (TextView) view.findViewById(R.id.buy_item);
        buyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateBuyFromMarketplace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DomainEmailActivity)requireActivity()).setHeaderText(getString(R.string.website_domain));
    }

    private void initiateBuyFromMarketplace() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        String status = "Loading. Please wait...";
        progressDialog.setMessage(status);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Intent intent = new Intent(requireContext(), UpgradeActivity.class);
        intent.putExtra("expCode", session.getFP_AppExperienceCode());
        intent.putExtra("fpName", session.getFPName());
        intent.putExtra("fpid", session.getFPID());
        intent.putExtra("loginid", session.getUserProfileId());
        if (session.getFPEmail() != null) {
            intent.putExtra("email", session.getFPEmail());
        } else {
            intent.putExtra("email", "ria@nowfloats.com");
        }
        if (session.getFPPrimaryContactNumber() != null) {
            intent.putExtra("mobileNo", session.getFPPrimaryContactNumber());
        } else {
            intent.putExtra("mobileNo", "9160004303");
        }
        intent.putExtra("profileUrl", session.getFPLogo());
        intent.putExtra("buyItemKey", "DOMAINPURCHASE");
        startActivity(intent);
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
        },1000);
    }

}