package gr.gdschua.bloodapp.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

import gr.gdschua.bloodapp.DatabaseAccess.DAOEvents;
import gr.gdschua.bloodapp.DatabaseAccess.DAOHospitals;
import gr.gdschua.bloodapp.Entities.Event;
import gr.gdschua.bloodapp.Entities.Hospital;
import gr.gdschua.bloodapp.R;

public class MapsFragment extends Fragment {

    private final DAOHospitals daoHospitals = new DAOHospitals();
    private final DAOEvents daoEvents = new DAOEvents();
    GoogleMap.OnMarkerClickListener MarkerClickListener = marker -> {
        MarkerInfoFragment myMarkerInfoFragment = new MarkerInfoFragment();
        if (Objects.equals(marker.getSnippet(), "Hospital")) {
            Hospital hospital = (Hospital) marker.getTag();

            Bundle bundle = new Bundle();
            assert hospital != null;
            bundle.putString("name", hospital.getName());
            bundle.putString("address", hospital.getAddress(getActivity()));
            bundle.putString("email", hospital.getEmail());
            myMarkerInfoFragment.setArguments(bundle);
            myMarkerInfoFragment.show(requireActivity().getSupportFragmentManager(), "My Fragment");

        } else if (Objects.equals(marker.getSnippet(), "Event")) {
            Event event = (Event) marker.getTag();

            Bundle bundle = new Bundle();
            assert event != null;
            bundle.putString("name", event.getName());
            bundle.putString("address", event.getAddress(getActivity()));
            daoHospitals.getUser(event.getOwner()).addOnCompleteListener(task -> {
                Hospital ownerHosp = task.getResult().getValue(Hospital.class);
                assert ownerHosp != null;
                bundle.putString("email", ownerHosp.getEmail());
                bundle.putString("organizer", ownerHosp.getName());
                myMarkerInfoFragment.setArguments(bundle);
                myMarkerInfoFragment.show(requireActivity().getSupportFragmentManager(), "My Fragment");
            });
            return true;

        }

        return true;
    };
    private GoogleMap map;
    private final ActivityResultLauncher<String> locationRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            handleLocation();
        } else {
            Toast.makeText(requireContext(), "We were not able to retrieve your location because you denied the permission.", Toast.LENGTH_LONG).show();
        }
    });
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            map = googleMap;
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                handleLocation();
            }


            googleMap.setOnMarkerClickListener(MarkerClickListener);
            placeMarkers();

        }
    };

    private void handleLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

                //Move the camera to the user's location and zoom in!
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
            });
            return;
        }


    }

    private void placeMarkers() {
        ArrayList<Event> events = daoEvents.getAllEvents();
        ArrayList<Hospital> hospitals = daoHospitals.getAllHospitals();

        if (events.size() > 0) {
            for (int i = 0; i < events.size(); i++) {
                LatLng eventLatLong = new LatLng(events.get(i).getLat(), events.get(i).getLon());
                map.addMarker(new MarkerOptions().position(eventLatLong).title(events.get(i).getName()).snippet("Event").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))).setTag(events.get(i));
            }
        }
        if (hospitals.size() > 0) {
            for (int i = 0; i < hospitals.size(); i++) {
                LatLng hospitalLatLong = new LatLng(hospitals.get(i).getLat(), hospitals.get(i).getLon());
                Objects.requireNonNull(map.addMarker(new MarkerOptions().position(hospitalLatLong).title(hospitals.get(i).getName()).snippet("Hospital"))).setTag(hospitals.get(i));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }


}