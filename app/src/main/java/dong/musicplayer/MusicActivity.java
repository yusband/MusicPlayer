package dong.musicplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MusicActivity extends AppCompatActivity {
    private Button previous_song, next_song, play_the_song, pause_the_song;

    private ListView musicList;// 暂停
    private List<Music_info> musicInfos;
    private Music_info musicInfo=new Music_info() ;
    private boolean isFirstTime=true;
    private boolean isPlaying=false;

    public boolean getIsPause() {
        return isPause;
    }

    public void setIsPause(boolean isPause) {
        this.isPause = isPause;
    }

    private boolean isPause=false;
    private int listPosition=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initButtons();
        setViewOnclickListener();
        musicList=(ListView)findViewById(R.id.music_list);
        musicList.setOnItemClickListener(new MusicListItemClickListener());
        musicInfos = getMusicInfos();
        setListAdpter(getMusicInfos());

    }
    private void setViewOnclickListener() {
        ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
        previous_song.setOnClickListener(viewOnClickListener);
        pause_the_song.setOnClickListener(viewOnClickListener);
        play_the_song.setOnClickListener(viewOnClickListener);
        next_song.setOnClickListener(viewOnClickListener);

    }
    private class ViewOnClickListener implements View.OnClickListener {
        Intent intent = new Intent(MusicActivity.this,MusicService.class);

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_music:
                    if (isFirstTime) {
//                        play_the_song.setBackgroundResource(R.mipmap.play_click);
                        Music_info musicInfo = musicInfos.get(0);
                        intent.putExtra("url", musicInfo.getUrl());
                        intent.putExtra("MSG", MusicConstants.MSG_START_MUSIC);
                        startService(intent);
                        isPlaying = true;
                        isFirstTime = false;

                        pause_the_song.setVisibility(View.VISIBLE);
                        play_the_song.setVisibility(View.GONE);

                    } else if (isPause) {

//                        play_the_song.setBackgroundResource(R.mipmap.play_click);
                        Music_info musicInfo = musicInfos.get(listPosition);
                        intent.putExtra("url", musicInfo.getUrl());
                        intent.putExtra("MSG", MusicConstants.MSG_START_MUSIC);
                        startService(intent);
                        isPause = true;
                        isPlaying = false;
                        pause_the_song.setVisibility(View.VISIBLE);
                        play_the_song.setVisibility(View.GONE);
                    }
                    break;
                case R.id.pause_music:
//                    pause_the_song.setBackgroundResource(R.mipmap.pause_click);
                    Music_info musicInfo = musicInfos.get(listPosition);
                    intent.putExtra("url", musicInfo.getUrl());
                    intent.putExtra("MSG", MusicConstants.MSG_PAUSE_MUSIC);
                    startService(intent);
                    isPause = true;
                    isPlaying = false;
                    pause_the_song.setVisibility(View.GONE);
                    play_the_song.setVisibility(View.VISIBLE);
                    break;

                case R.id.next_music:
                    next();
                    break;
                case R.id.previous_music:
                    previous();
                    break;
            }

            }
        }


    //
    public void initButtons() {
        previous_song = (Button) findViewById(R.id.previous_music);
        next_song = (Button) findViewById(R.id.next_music);
        play_the_song = (Button) findViewById(R.id.play_music);
        pause_the_song = (Button) findViewById(R.id.pause_music);
        pause_the_song.setVisibility(View.GONE);
    }

private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if(musicInfos != null){
            listPosition=position;
            Music_info musicInfo = musicInfos.get(position);
            Log.d("mp3Info-->", musicInfo.toString());
            Intent intent = new Intent();
            intent.putExtra("url", musicInfo.getUrl());
            intent.putExtra("MSG", MusicConstants.MSG_START_MUSIC);
            intent.setClass(MusicActivity.this, MusicService.class);
            startService(intent);       //启动服务
            pause_the_song.setVisibility(View.VISIBLE);
            play_the_song.setVisibility(View.GONE);
            isPlaying=true;
            isPause=false;
        }
    }
}

    public List<Music_info> getMusicInfos() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Music_info>music_infos = new ArrayList<Music_info>();
        for (int i = 0; i < cursor.getCount(); i++) {
            Music_info music_info = new Music_info();
            cursor.moveToNext();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));              //文件路径
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐
            if (isMusic != 0) {     //只把音乐添加到集合当中
                music_info.setId(id);
                music_info.setTitle(title);
                music_info.setArtist(artist);
                music_info.setDuration(duration);
                music_info.setSize(size);
                music_info.setUrl(url);
                music_infos.add(music_info);
            }
        }
        return music_infos;
    }
    public void setListAdpter(List<Music_info> music_infos) {
        List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator =music_infos.iterator(); iterator.hasNext();) {
            Music_info music_info = (Music_info) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", music_info.getTitle());
            map.put("Artist", music_info.getArtist());
            map.put("duration", String.valueOf(music_info.getDuration()));
            map.put("size", String.valueOf(music_info.getSize()));
            map.put("url", music_info.getUrl());
            mp3list.add(map);
        }
        SimpleAdapter mAdapter = new SimpleAdapter(this, mp3list,
                R.layout.music_list_item, new String[]{ "title", "Artist", "duration" },
                new int[] { R.id.music_title, R.id.music_Artist, R.id.music_duration });
        musicList.setAdapter(mAdapter);
    }
public void next()
{if (listPosition<musicInfos.size()-1){
    listPosition++;
    Intent intent = new Intent(MusicActivity.this,MusicService.class);
    Music_info musicInfo = musicInfos.get(listPosition);
    intent.putExtra("url", musicInfo.getUrl());
    intent.putExtra("MSG", MusicConstants.MSG_START_MUSIC);
    startService(intent);
    isPlaying = true;
    isFirstTime = false;

    pause_the_song.setVisibility(View.VISIBLE);
    play_the_song.setVisibility(View.GONE);}
    else{
    Toast.makeText(MusicActivity.this,"这已经是最后一首歌",Toast.LENGTH_LONG).show();
    }

}
public void previous()
{
if (listPosition>=1){
    listPosition--;
    Intent intent = new Intent(MusicActivity.this,MusicService.class);
    Music_info musicInfo = musicInfos.get(listPosition);
    intent.putExtra("url", musicInfo.getUrl());
    intent.putExtra("MSG", MusicConstants.MSG_START_MUSIC);
    startService(intent);
    isPlaying = true;
    isFirstTime = false;

    pause_the_song.setVisibility(View.VISIBLE);
    play_the_song.setVisibility(View.GONE);}
    else{
    Toast.makeText(MusicActivity.this,"这已经是第一首歌",Toast.LENGTH_LONG).show();
    }
}


}
