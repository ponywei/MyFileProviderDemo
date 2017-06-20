package com.pony.learn.myfileproviderdemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity {
	private static final int REQUEST_CODE_GALLERY = 101;
	private static final int REQUEST_CODE_CAPTURE = 100;

	public static final String APP_ACTION_NAME = "com.pony.learn.myfileproviderdemo.CAPTURE";
	public static final String FILE_PROVIDER_AUTHORITY = "com.pony.learn.myfileproviderdemo.provider";

	private String currentPhotoPath;
	private ImageView imageView;

	private boolean isOtherAppStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				requestCheckPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
			}
		});

		imageView = (ImageView) findViewById(R.id.main_image_view);

		//从其它app start过来
		if (getIntent() != null && APP_ACTION_NAME.equals(getIntent().getAction())) {
			isOtherAppStart = true;
			actionCaptureImage();
		}
	}

	private void actionCaptureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) != null) {
			// EXACTLY 使用指定的路径保存照片
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (photoFile != null) {
				Uri photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
				startActivityForResult(intent, REQUEST_CODE_CAPTURE);
			}
		}
	}

	/**
	 * 创建本地保存照片的私有路径
	 * @return FileDir
	 * @throws IOException e
	 */
	private File createImageFile() throws IOException{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,	/* prefix */
				".jpg",			/* suffix */
				storageDir		/* directory */
		);

		//更新全局的当前图片路径
		currentPhotoPath = image.getAbsolutePath();
		return image;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			Toast.makeText(this, "nothing.", Toast.LENGTH_SHORT).show();
			return;
		}

		if (requestCode == REQUEST_CODE_CAPTURE && !TextUtils.isEmpty(currentPhotoPath)) {
			if (isOtherAppStart) {
				// 如果从其它app发起，将当前拍摄图片的Uri返回，并授予其私有文件的临时访问权限
				Intent intent = new Intent();
				Uri imageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, new File(currentPhotoPath));
				intent.setData(imageUri);
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				setResult(RESULT_OK, intent);
				finish();
				return;
			}

			imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
		}
	}

	@Override
	protected void onPermissionsGranted(int requestCode) {
		super.onPermissionsGranted(requestCode);
		actionCaptureImage();
	}

	@Override
	protected void onPermissionsDenied(int requestCode) {
		super.onPermissionsDenied(requestCode);
		Toast.makeText(this, "不给权限怎么玩儿！", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
