package com.zapzvon.dima.zapiszvon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // список аттрибутов групп для чтения
    String groupFrom[];
    // список ID view-элементов, в которые будет помещены аттрибуты групп
    int groupTo[];

    // список аттрибутов элементов для чтения
    String childFrom[];
    // список ID view-элементов, в которые будет помещены аттрибуты элементов
    int childTo[];

    ArrayList<File> spis;
    ExpandableListView lvMain;
    SimpleExpandableListAdapter adapter=null;
    ArrayList<ArrayList<Map<String, String>>> childData = new ArrayList<>();
    ArrayList<Map<String, String>> groupData = new ArrayList<>();

    public TextView startTimeField,endTimeField;
    private MediaPlayer mediaPlayer=null;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    private SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, newInstance(1))
                .commit();

        if (WalkingIconService.Ser==null)
            startService(new Intent(this, WalkingIconService.class));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (adapter!=null) {
            switch (position) {
                case 0:
                    mTitle = getString(R.string.title_section1);
                    SpisMap(spis, groupData, childData, "name");
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    mTitle = getString(R.string.title_section2);
                    SpisMap(spis, groupData, childData, "data");
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    mTitle = getString(R.string.title_section3);
                    SpisMap(spis, groupData, childData, "tip");
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    public void onSectionAttached(int number) {

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_example) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void play(View view){
        if (mediaPlayer!=null) {
            mediaPlayer.start();
            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();

            seekbar.setMax((int) finalTime);


            endTimeField.setText(String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) finalTime)))
            );
            startTimeField.setText(String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            startTimeField.setText(String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    public void pause(View view){
        if (mediaPlayer!=null)
            mediaPlayer.pause();
    }

    public void forward(View view){
        if (mediaPlayer!=null) {
            int temp = (int) startTime;
            int forwardTime = 5000;
            if ((temp + forwardTime) <= finalTime) {
                startTime = startTime + forwardTime;
                mediaPlayer.seekTo((int) startTime);
            }
        }
    }

    public void rewind(View view){
        if (mediaPlayer!=null) {
            int temp = (int) startTime;
            int backwardTime = 5000;
            if ((temp - backwardTime) > 0) {
                startTime = startTime - backwardTime;
                mediaPlayer.seekTo((int) startTime);
            }
        }
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void SpisMap(ArrayList<File> list,
                             ArrayList<Map<String, String> > groupData,
                             ArrayList<ArrayList<Map<String, String> > > childData, String tip)
    {
        groupData.clear();
        childData.clear();

        //Sorting
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File fruite1, File fruite2) {

                return fruite1.getAbsolutePath().compareTo(fruite2.getAbsolutePath());
            }
        });

        Map<String, ArrayList<ArrayList<String> > > Item;

        Map<String, String> m;

        Item=new TreeMap<>();

        for (File fil:list)
        {

            String[] p=fil.getName().replace(".3gpp", "").split("_");
            p[2]=p[2].replace("-",":");
            String kl = null;
            if (tip.equals("name"))
                kl=p[0];
            if (tip.equals("data"))
                kl=p[1];
            if (tip.equals("tip")) {
                if (p.length>4) {
                    if (p[4].equals("in"))
                        kl="Входящие";
                    else
                        kl="Исходящие";
                }
                else
                {
                    kl = "";
                }
            }
            ArrayList<ArrayList<String> > v;
            if (Item.containsKey(kl))
            {
                v=Item.get(kl);
            }
            else
            {
                v=new ArrayList<>();
                v.add(new ArrayList<String>());
                v.add(new ArrayList<String>());
                v.add(new ArrayList<String>());
                v.add(new ArrayList<String>());
                v.add(new ArrayList<String>());
                v.add(new ArrayList<String>());
            }

            v.get(0).add(p[0]);
            v.get(1).add(p[1]);
            v.get(2).add(p[2]);
            if (p.length>3) {
                if (p[3].contains(":"))
                {
                    fil.renameTo(new File(fil.getAbsolutePath().replace(":","-")));
                }
                p[3]=p[3].replace("-",":");
                v.get(3).add(p[3]);
                if (p.length>4) {
                    if (p[4].equals("in"))
                        v.get(4).add("Вх");
                    else
                        v.get(4).add("Исх");
                }
                else
                {
                    v.get(4).add("");
                }
            }
            else {
                String fg = null;
                try {
                    releaseMP();
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(fil.getPath());
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    finalTime = mediaPlayer.getDuration();
                    mediaPlayer.release();

                    fg=String.format("%d-%d",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) finalTime)));
                    String s1=fil.getParent();
                    String s2=fil.getName().replace(".3gpp","_")+fg+".3gpp";

                    fil.renameTo(new File(s1,s2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                v.get(3).add(fg);
                v.get(4).add("");
            }
            v.get(5).add(fil.getAbsolutePath());
            Item.put(kl,v);
        }

        ArrayList<Map<String, String>> childDataItem;

        if (Item.size()>0) {
            for (String group : Item.keySet()) {
                // заполняем список аттрибутов для каждой группы
                m = new HashMap<>();
                m.put("groupName", group); // имя компании
                groupData.add(m);

                // создаем коллекцию элементов для первой группы
                childDataItem = new ArrayList<Map<String, String>>();
                // заполняем список аттрибутов для каждого элемента
                for (int i = 0; i < Item.get(group).get(0).size(); i++) {
                    m = new HashMap<String, String>();
                    m.put("name", Item.get(group).get(0).get(i)); // название телефона
                    m.put("data", Item.get(group).get(1).get(i)); // название телефона
                    m.put("time", Item.get(group).get(2).get(i)); // название телефона
                    m.put("dlit", Item.get(group).get(3).get(i)); // название телефона
                    m.put("tip", Item.get(group).get(4).get(i)); // название телефона
                    m.put("put", Item.get(group).get(5).get(i)); // название телефона
                    childDataItem.add(m);
                }
                // добавляем в коллекцию коллекций
                childData.add(childDataItem);
            }
        }
        else
        {
            m = new HashMap<>();
            m.put("groupName", "Записей ещё нет"); // имя компании
            groupData.add(m);

            // создаем коллекцию элементов для первой группы
            childDataItem = new ArrayList<>();
            // заполняем список аттрибутов для каждого элемента

            m = new HashMap<>();
            m.put("name", "Нет записей"); // название телефона
            m.put("data", ""); // название телефона
            m.put("time", ""); // название телефона
            m.put("dlit", ""); // название телефона
            m.put("tip", ""); // название телефона
            m.put("put", ""); // название телефона
            childDataItem.add(m);

            // добавляем в коллекцию коллекций
            childData.add(childDataItem);
        }
    }

    private void listFile(File F,ArrayList<File> list)
    {
        if (F.exists()) {

            File[] fList;

            fList = F.listFiles();

            for (File aFList : fList) {
                //Нужны только папки в место isFile() пишим isDirectory()
                if (aFList.isFile()) {
                    list.add(aFList);
                }
                if (aFList.isDirectory())
                    listFile(aFList, list);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressLint("ValidFragment")
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Indicate that this fragment would like to influence the set of actions in the action bar.
            // находим список

            startTimeField =(TextView)findViewById(R.id.textView1);
            endTimeField =(TextView)findViewById(R.id.textView2);
            seekbar = (SeekBar)findViewById(R.id.seekBar1);

            seekbar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mediaPlayer!=null) {
                        mediaPlayer.seekTo(seekbar.getProgress());
                        mediaPlayer.start();
                    }
                    return false;
                }
            });

            File dirZv = Environment.getExternalStorageDirectory();
            dirZv = new File(dirZv, "ZapZvon");
            dirZv.mkdirs();

            spis = new ArrayList<>();

            listFile(dirZv,spis);

            lvMain = (ExpandableListView) getActivity().findViewById(R.id.expandableListView);

            SpisMap(spis, groupData, childData, "name");

            // список аттрибутов групп для чтения
            groupFrom = new String[] {"groupName"};
            // список ID view-элементов, в которые будет помещены аттрибуты групп
            groupTo = new int[] {R.id.textview_spis_1};

            // список аттрибутов элементов для чтения
            childFrom = new String[] {"name","data","time","dlit","tip","put"};
            // список ID view-элементов, в которые будет помещены аттрибуты элементов
            childTo = new int[] {R.id.textview_1,R.id.textview_2,R.id.textview_3,R.id.textview_4,R.id.textview_5};

            adapter = new SimpleExpandableListAdapter(
                    getActivity(),
                    groupData,
                    R.layout.my_list_spis,
                    groupFrom,
                    groupTo,
                    childData,
                    R.layout.my_list_item,
                    childFrom,
                    childTo);

            lvMain.setAdapter(adapter);

            registerForContextMenu(lvMain);

            if (childData.size()>0)
            // нажатие на элемент

                lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });

            lvMain.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition,   int childPosition, long id) {
                    File dirZv = Environment.getExternalStorageDirectory();
                    dirZv = new File(dirZv, "ZapZvon");
                    dirZv = new File(getChildText(groupPosition,childPosition));

                    try {
                        releaseMP();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(dirZv.getPath());
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.prepare();
                        play(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return false;
                }
            });
        }

        String getChildText(int groupPos, int childPos) {
            return ((Map<String,String>)(adapter.getChild(groupPos, childPos))).get("put");
        }

        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(0, 1, 0, "Удалить");
        }

        public boolean onContextItemSelected(MenuItem menuItem) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();

            int groupPos = 0, childPos = 0;

            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
            {
                groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
                switch (menuItem.getItemId())
                {
                    case 1:
                        File dirZv;
                        dirZv = new File(getChildText(groupPos,childPos));
                        dirZv.delete();

                        childData.get(groupPos).remove(childPos);

                        if (childData.get(groupPos).size()==0)
                            groupData.remove(groupPos);

                        adapter.notifyDataSetChanged();

                        return true;

                    default:
                        return super.onContextItemSelected(menuItem);
                }
            }

            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
            {
                groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                switch (menuItem.getItemId())
                {
                    case 1:
                        File dirZv;
                        int si=childData.get(groupPos).size();
                        for(int i=0;i<si;i++) {
                            dirZv = new File(getChildText(groupPos, 0));
                            dirZv.delete();

                            childData.get(groupPos).remove(0);

                            if (childData.get(groupPos).size() == 0)
                                groupData.remove(groupPos);
                        }

                        adapter.notifyDataSetChanged();

                        return true;

                    default:
                        return super.onContextItemSelected(menuItem);
                }
            }


            return super.onContextItemSelected(menuItem);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
