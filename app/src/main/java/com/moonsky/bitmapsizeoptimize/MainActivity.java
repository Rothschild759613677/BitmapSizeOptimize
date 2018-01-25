package com.moonsky.bitmapsizeoptimize;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.UUID;

/**
 * @author nick
 * @date 2018-01-24
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etInput;
    private TextView tvOutput;
    private ImageView ivShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInput = (EditText) findViewById(R.id.et_input_src);
        tvOutput = (TextView) findViewById(R.id.tv_output_src);
        ivShow = (ImageView) findViewById(R.id.iv_show);
        findViewById(R.id.btn_compress).setOnClickListener(this);
        findViewById(R.id.btn_native_compress).setOnClickListener(this);
        findViewById(R.id.btn_show).setOnClickListener(this);

        etInput.setText(Constant.ROOT_DIR + "a.jpg");
    }


    @Override
    public void onClick(View v) {
        String sourcePath = etInput.getText().toString().trim();
        switch (v.getId()) {
            case R.id.btn_compress:
                long startTime = SystemClock.currentThreadTimeMillis();
                if (!TextUtils.isEmpty(sourcePath)) {
                    try {
                        String targetFilePath = BitmapUtils.compressBitmap(sourcePath, 1080, 1640,
                                "compress-bitmap.jpg", Bitmap.CompressFormat.JPEG, 80);

                        tvOutput.setText(targetFilePath);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                long endTime = SystemClock.currentThreadTimeMillis();

                Toast.makeText(this, "system compress bitmap success-" + (endTime - startTime), Toast.LENGTH_SHORT).show();
                Log.d("Nick", "bitmap compress time---" + (endTime - startTime));
                break;
            case R.id.btn_show:
                ivShow.setImageBitmap(BitmapUtils.getMemoryBitmap(sourcePath, 1080, 1640));
                break;
            case R.id.btn_native_compress:

                long nativeStartTime = SystemClock.currentThreadTimeMillis();

                Bitmap memoryBitmap = BitmapUtils.getMemoryBitmap(sourcePath, 1080, 1640);
                boolean state = BitmapUtils.nativeCompressBitmap(memoryBitmap, 80, Constant.ROOT_DIR + UUID.randomUUID().toString() + ".jpg");

                long nativeEndTime = SystemClock.currentThreadTimeMillis();

                long diffValue = nativeEndTime - nativeStartTime;
                if (state) {
                    Toast.makeText(this, "native compress bitmap success-" + diffValue, Toast.LENGTH_SHORT).show();
                }
                Log.d("Nick", "bitmap compress time---" + diffValue);
                break;
            default:
                break;
        }
    }
}
