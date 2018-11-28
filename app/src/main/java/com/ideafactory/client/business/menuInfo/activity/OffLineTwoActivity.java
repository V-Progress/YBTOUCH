package com.ideafactory.client.business.menuInfo.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.FileTool;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class OffLineTwoActivity extends BaseActivity {
    private TextView stepOne1, stepOne2;
    private TextView stepTwo1, stepTwo2;
    private TextView stepThree1, stepThree2;
    private Button importBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line_two);

        initView();
        setView();
    }

    private void initView() {
        stepOne1 = (TextView) findViewById(R.id.tv_step_one1);
        stepOne2 = (TextView) findViewById(R.id.tv_step_one2);
        stepTwo1 = (TextView) findViewById(R.id.tv_step_two1);
        stepTwo2 = (TextView) findViewById(R.id.tv_step_two2);
        stepThree1 = (TextView) findViewById(R.id.tv_step_three1);
        stepThree2 = (TextView) findViewById(R.id.tv_step_three2);
        importBtn = (Button) findViewById(R.id.btn_offline_two_import);
    }

    private void setView() {
        stepOne1.setText("01");
        stepOne2.setText(R.string.input_step_one);
        stepTwo1.setText("02");
        stepTwo2.setText(R.string.input_step_two);
        stepThree1.setText("03");
        stepThree2.setText(R.string.input_step_three);
        importBtn.setOnClickListener(importListener);
    }

    private List<String> fileNames = new ArrayList<String>();
    private String[] items;
    private String zipFile;
    View.OnClickListener importListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //获得指定路径下的所有zip文件
            String outerPath = TYTool.getOuterPath();
            if (TextUtils.isEmpty(outerPath)) {
                Toast.makeText(OffLineTwoActivity.this, R.string.check_usb, Toast.LENGTH_SHORT).show();
                return;
            }
            Vector vector = ZipUtil.GetFileName(outerPath, ".zip");
            fileNames.clear();
            for (int i = 0; i < vector.size(); i++) {
                String s = vector.get(i).toString();
                fileNames.add(i, s);
            }
            items = fileNames.toArray(new String[fileNames.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(OffLineTwoActivity.this);
            builder.setTitle(R.string.select_zip)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           zipFile = items[which];
                            Toast.makeText(OffLineTwoActivity.this, R.string.unzip, Toast.LENGTH_SHORT).show();
                            ThreadUitls.runInThread(new Runnable() {
                                @Override
                                public void run() {
                                    UnZip(zipFile);
                                }
                            });
                        }
                    }).create().show();
        }
    };
    String targetPath;

    //解压zip文件
    private void UnZip(String zipFile) {
        try {
            File zipFil = new File(TYTool.getOuterPath() + "/" + zipFile);//被解压缩的文件目录
            targetPath = Environment.getExternalStorageDirectory().getPath() + "/mnt/sdcard/layout/";//目标目录
            ZipUtil.unZipFiles(zipFil, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File layoutFile = new File(targetPath + "layout.txt");
        File delFile = new File(Environment.getExternalStorageDirectory().getPath() + "/mnt/sdcard/layout");
        if (layoutFile.exists()) {
            //导入布局
            try {
                InputStream inputStream = new FileInputStream(targetPath + "layout.txt");
                String string = TYTool.getString(inputStream);
                LayoutCache.putLayoutPosition("1");
                LayoutCache.putLayoutCache(string);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OffLineTwoActivity.this, R.string.zip_fail, Toast.LENGTH_SHORT).show();
                }
            });
            if (delFile.exists()) {
                FileTool.delete(delFile);
            }
            return;
        }
        //复制资源
        FileTool.copy(targetPath, ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH);
        //删除解压的文件
        if (delFile.exists()) {
            FileTool.delete(delFile);
        }
        //完成后重启APP
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialog progressDialog = TYTool.coreInfoShow3sDialog();
                progressDialog.setTitle(R.string.restart_app);
                progressDialog.setMessage(getResources().getString(R.string.restart_three_seconds));
                progressDialog.show();
                TYTool.AppRestart.start();
            }
        });
    }

    public void sysTo(View view) {
        finish();
    }
}
