package io.conex.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.conex.app.datamodels.CategoryItem;
import io.conex.app.datamodels.FilterContainer;
import io.conex.app.datamodels.Mode;
import io.conex.app.fragments.CategoryFragment;
import io.conex.app.fragments.DevicesFragment;
import io.conex.app.fragments.OverviewFragment;
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.JsonUtil;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Device;
import io.swagger.client.model.Devices;
import io.swagger.client.model.Function;
import io.swagger.client.model.Ids;

public class DevicesActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private DefaultApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        // do not rotate!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences sharedPref = this.getSharedPreferences(this.getString(R.string.preferences_file_key), MODE_PRIVATE);
        String url = sharedPref.getString(this.getString(R.string.api_url_key), null);

        api = new DefaultApi();
        api.setBasePath(url);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.drawable.logo_styled);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                int position = tab.getPosition();
                Log.d("ui","Tab "+tab.getPosition()+" selected");
                final Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(position);

                switch (position) {
                    case 1:
                        requestCategoryIds(FilterContainer.getInstance(), (CategoryFragment) fragment);
                        break;
                    case 2:
                        requestDevices(FilterContainer.getInstance(), (DevicesFragment) fragment);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("ui","Tab "+tab.getPosition()+" unselected");

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void requestCategoryIds(final FilterContainer filter, final CategoryFragment fragment) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("api", "requesting "+filter.getMode().name().toLowerCase()+" ids");
                switch(filter.getMode()) {
                    case FUNCTIONS:
                        try {
                            Ids ids = api.functionsPost(filter.getPureFilter());
                            ArrayList<CategoryItem> items = new ArrayList<CategoryItem>();
                            for (String id : ids.getIds()) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.FUNCTIONS));
                            }
                            deliverIds(items);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ROOMS:
                        try {
                            Ids ids = api.roomsPost(filter.getPureFilter());
                            ArrayList<CategoryItem> items = new ArrayList<CategoryItem>();
                            for (String id : ids.getIds()) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.ROOMS));
                            }
                            deliverIds(items);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case GROUPS:
                        try {
                            Ids ids = api.groupsPost(filter.getPureFilter());
                            ArrayList<CategoryItem> items = new ArrayList<CategoryItem>();
                            for (String id : ids.getIds()) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.GROUPS));
                            }
                            deliverIds(items);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ALL:
                        try {
                            Devices devices = api.devicesPost(filter.getPureFilter());
                            HashSet<String> functions = new HashSet<>();
                            HashSet<String> rooms = new HashSet<>();
                            HashSet<String> groups = new HashSet<>();
                            for (Device d : devices.getDevices()) {
                                for (Function f : d.getFunctions()) {
                                    f.setFunctionId(JsonUtil.getFunctionId(f));
                                    functions.add(f.getFunctionId());
                                }
                                rooms.addAll(d.getRoomIds());
                                groups.addAll(d.getGroupIds());
                            }
                            ArrayList<CategoryItem> items = new ArrayList<CategoryItem>();
                            for (String id : functions) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.FUNCTIONS));
                            }
                            for (String id : groups) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.GROUPS));
                            }
                            for (String id : rooms) {
                                if (id == null) continue;
                                items.add(new CategoryItem(id, Mode.ROOMS));
                            }
                            deliverIds(items);
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }

            private void deliverIds(final List<CategoryItem> ids) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("api", "received "+filter.getMode().name().toLowerCase()+" ids, now updating CategoryFragment list with "+ids.size()+" categories");
                        fragment.setCategoryList(ids);
                    }
                });
            }
        }).start();
    }

    private void requestDevices(final FilterContainer filter, final DevicesFragment fragment) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.d("api", "requesting devices with filter "+ ApiInvoker.serialize(filter.getPureFilter()).toString());
                    Devices devices = api.devicesPost(filter.getPureFilter());
                    for (Device d : devices.getDevices()) {
                        for (Function f : d.getFunctions()) {
                            f.setFunctionId(JsonUtil.getFunctionId(f));
                        }
                    }
                    deliverDevices(devices.getDevices());
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }

            private void deliverDevices(final List<Device> devices) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("api", "received "+devices.size()+" devices");
                        fragment.update(devices);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(DevicesActivity.this, MainActivity.class);
                myIntent.putExtra("coming_from_devices_activity", true); //Optional parameters
                DevicesActivity.this.startActivity(myIntent);
                return true;
            case R.id.action_reload:
                int tab = mViewPager.getCurrentItem();

                switch (tab) {
                    case 1:
                        //((CategoryFragment) mSectionsPagerAdapter.getRegisteredFragment(tab)).updateListView();
                        break;
                    case 2:
                        //((DevicesFragment) mSectionsPagerAdapter.getRegisteredFragment(tab)).updateListView();
                        break;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        private SparseArray<Fragment> registeredFragments = new SparseArray<>();

        private Mode filterMode;
        private String filterCategoryId;

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch(position) {
                case 0:
                    return OverviewFragment.newInstance(position + 1, mViewPager);
                case 1:
                    return CategoryFragment.newInstance(position + 1, mViewPager);
                case 2:
                    return DevicesFragment.newInstance(position + 1, mViewPager);
                default:
                    return DevicesFragment.newInstance(position + 1, mViewPager);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Overview";
                case 1:
                    return "Category";
                case 2:
                    return "Devices";
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
