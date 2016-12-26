package com.huishangyun.Channel.Competing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.huishangyun.Activity.BaseActivity;
import com.huishangyun.App.MyApplication;
import com.huishangyun.Channel.Clues.ClueCustomToast;
import com.huishangyun.Channel.Orders.Common;
import com.huishangyun.Channel.Plan.CustomersListActivity;
import com.huishangyun.Channel.Plan.SortModel;
import com.huishangyun.Channel.Visit.BitmapTools;
import com.huishangyun.Channel.Visit.PhotoHelpDefine;
import com.huishangyun.Channel.Visit.VisitPhotoManage;
import com.huishangyun.Map.LocationUtil;
import com.huishangyun.Util.Base64Util;
import com.huishangyun.Util.DataUtil;
import com.huishangyun.Util.JsonUtil;
import com.huishangyun.Util.L;
import com.huishangyun.Util.ZJRequest;
import com.huishangyun.manager.CompetingManager;
import com.huishangyun.model.CallSystemApp;
import com.huishangyun.model.Constant;
import com.huishangyun.model.Content;
import com.huishangyun.model.Methods;
import com.huishangyun.task.UpLoadFileTask.OnUpLoadResult;
import com.huishangyun.task.UpLoadImgSignText;
import com.huishangyun.yun.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 新增竞品页面
 * 
 * @熊文娟
 * 
 */
public class CompetingNewAdd extends BaseActivity implements OnUpLoadResult{
	
	protected static final String TAG = null;
	private LinearLayout backLayout=null;//返回
	private ImageView image1;//第一张图片
	private ImageView image2;//第二张图片
	private ImageView image3;//第三张图片
	private RelativeLayout competing_client_layout;//选择客户
	private TextView txt_competing_clientName;//客户
	private EditText txt_competing_brandName;//品牌
	private EditText edit_competing_productName;//本品
	private EditText edit_competing_content;//描述
	private TextView txt_competing_submit;//提交
	ArrayList<SortModel> arrayList = new ArrayList<SortModel>();
	public static List<String> Competing_Img_List = new ArrayList<String>();
	public static List<String> Competing_pList = new ArrayList<String>();//图片地址存储
	Calendar calendar;
	private String time;//时间
	private String FileName ;//文件地址
	private String photoPath;//图片绝对路径
	ProgressDialog pDialog;//进度条
	private CompetingList list = new CompetingList();
	private Float Lng;//经度
	private Float Lat;//维度
	private String location;//地理位置
	private String Brand;//品牌
	private String Product;////本品
	private int Member_ID;//客户id
	private String MemberName;//客户名称
	CompetingHostoryList competingList = new CompetingHostoryList();
	private int ManagerID;
	private String newSDpath;//压缩后的sd卡路径
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_competing_new);
		ManagerID = MyApplication.getInstance().getManagerID();
		competingList.setManagerID(ManagerID);
		//接收传值
		Intent intent = getIntent();
		Member_ID = intent.getIntExtra("Member_ID", 0);
		MemberName = intent.getStringExtra("MemberName");
		initView();
	}
	
	private void getNetData(final int Member_ID, final String Brand,final String Product,final String Note,
			final Float Lng,final Float Lat, final String Loc, final String Picture,
			final int Manager_ID) {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				String result = DataUtil.callWebService(
						Methods.COMPETING_NEW,
						getJson(Member_ID,Brand,Product, Note, Lng, Lat,
								Loc, Picture, Manager_ID));
				Log.e(TAG,"提交数据："+ getJson(Member_ID, Brand,Product,Note, Lng, Lat, Loc, Picture, Manager_ID));
				// 先判断网络数据是否获取成功，防止网络不好导致程序崩溃
				if (result != null) {
					// 获取对象的Type
					try {
						JSONObject jsonObject = new JSONObject(result);
						// Log.e(TAG, "code:" + jsonObject.getInt("Code"));
						int code = jsonObject.getInt("Code");
						Log.e(TAG, "code------:" + code);
						// 假如code返回为0则表示删除成功,否则为失败
						if (code == 0) {
							mHandler.sendEmptyMessage(0);
						} else {
							mHandler.sendEmptyMessage(3);
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					mHandler.sendEmptyMessage(2);

				}

			}
		}.start();
	}

	/**
	 * 设置json对象
	 * 
	 * @param
	 * @return
	 */
	private String getJson(int Member_ID,String Brand,String Product,
			String Note, Float Lng, Float Lat, String Loc,
			String Picture, int Manager_ID) {

		list.setAction("Insert");
		// id新增传入0
		list.setID(0);
		list.setMember_ID(Member_ID);
		list.setBrand(Brand);
		list.setProduct(Product);
		list.setNote(Note);
		list.setLng(Lng);
		list.setLat(Lat);
		list.setLoc(Loc);
		list.setPicture(Picture);
		list.setManager_ID(Manager_ID);
		ZJRequest<CompetingList> zjRequest = new ZJRequest<CompetingList>();
		zjRequest.setData(list);
		return JsonUtil.toJson(zjRequest);
		
	}
	
	/**
	 * 初始化组件
	 */
	private void initView() {
		calendar = Calendar.getInstance();
		//启动定位获取地理信息
		LocationUtil.startLocation(CompetingNewAdd.this, mLocationListener);
		//获取资源
		backLayout=(LinearLayout) findViewById(R.id.competing_new_back);
		image1 = (ImageView) findViewById(R.id.img_competing_new_photo1);
		image2 = (ImageView) findViewById(R.id.img_competing_new_photo2);
		image3 = (ImageView) findViewById(R.id.img_competing_new_photo3);
		competing_client_layout = (RelativeLayout) findViewById(R.id.competing_client_layout);
		txt_competing_clientName = (TextView) findViewById(R.id.txt_competing_clientName);
		txt_competing_brandName = (EditText) findViewById(R.id.txt_competing_brandName);
		edit_competing_productName = (EditText) findViewById(R.id.edit_competing_productName);
		edit_competing_content = (EditText) findViewById(R.id.edit_competing_content);
		txt_competing_submit = (TextView) findViewById(R.id.txt_competing_submit);
		
		
		//初始化数据
		Competing_Img_List.clear();
		Competing_pList.clear();
		// 所有ImageView重置
		image1.setImageResource(R.drawable.visit_img);
		image2.setImageResource(R.drawable.visit_img);
		image3.setImageResource(R.drawable.visit_img);
		
		try {
			if (!MemberName.equals("无")) {
				txt_competing_clientName.setText(MemberName);
			}else {
				showImage();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			L.e("ui数据获取失败");
		}
		
		
		//添加监听事件
		backLayout.setOnClickListener(new myOnClickListener());
		image1.setOnClickListener(new myOnClickListener());
		image2.setOnClickListener(new myOnClickListener());
		image3.setOnClickListener(new myOnClickListener());
		competing_client_layout.setOnClickListener(new myOnClickListener());
		txt_competing_submit.setOnClickListener(new myOnClickListener());
	}
	
	/**
	 * 单击事件处理
	 * @author xsl
	 *
	 */
	private class myOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.competing_new_back:// 返回
				competingList.setIsSubmit(0);
				saveData();
				finish();
				break;
			case R.id.competing_client_layout://客户选择
				
				Intent intent = new Intent(CompetingNewAdd.this, CustomersListActivity.class);
				//选择个客户/人员界面，客户传值“0”，人员传值“1”（注意传String类型）
				intent.putExtra("mode", "0");
				//多选传0，单选传1
				intent.putExtra("select", 1);
				//传递分组名称
				intent.putExtra("groupName", "分类");
				intent.putExtra("Tittle", "选择客户");
				startActivityForResult(intent, 4);
				
				break;
			case R.id.img_competing_new_photo1:// 照片1
//				T.showShort(CompetingNewAdd.this, "拍照1");
				if (Competing_Img_List.size() >= 1) {
					Intent intent1 = new Intent(CompetingNewAdd.this,VisitPhotoManage.class);
					intent1.putExtra("index", 3);
					intent1.putExtra("imgselect", 0);
					startActivityForResult(intent1, 1);
				}else {
					
					FileName = getSystemTime() + ".jpg";
					photoPath = Constant.SAVE_IMG_PATH + File.separator
							+ FileName;
					CallSystemApp.callCamera(CompetingNewAdd.this, photoPath);
					
				}
				
				break;
			case R.id.img_competing_new_photo2:// 照片2
				
				if (Competing_Img_List.size() >= 2) {
					Intent intent1 = new Intent(CompetingNewAdd.this,VisitPhotoManage.class);
					intent1.putExtra("index", 3);
					intent1.putExtra("imgselect", 1);
					startActivityForResult(intent1, 1);
				}else {
					
					FileName = getSystemTime() + ".jpg";
					photoPath = Constant.SAVE_IMG_PATH + File.separator
							+ FileName;
					CallSystemApp.callCamera(CompetingNewAdd.this, photoPath);
					
				}
				break;
			case R.id.img_competing_new_photo3:// 照片3
				
				if (Competing_Img_List.size() >= 3) {
					Intent intent1 = new Intent(CompetingNewAdd.this,VisitPhotoManage.class);
					intent1.putExtra("index", 3);
					intent1.putExtra("imgselect", 2);
					startActivityForResult(intent1, 1);
				}else {
					
					FileName = getSystemTime() + ".jpg";
					photoPath = Constant.SAVE_IMG_PATH + File.separator
							+ FileName;
					CallSystemApp.callCamera(CompetingNewAdd.this, photoPath);
					
				}
	            break;
			case R.id.txt_competing_submit://提交数据
				
				 //获取登录人id
			    int Manager_ID  = Integer.parseInt(preferences.getString(Constant.HUISHANGYUN_UID,"0"));
			    if (arrayList.size()>0) {
			    	Member_ID = arrayList.get(0).getID();//客户编号
				}
			    String Note = edit_competing_content.getText().toString();//描述
			    //图片地址拼接
				 StringBuffer mangerid = new StringBuffer("");
					for (int i = 0; i < Competing_pList.size(); i++) {
						if (i>0) {
							mangerid.append("#");
						}
						mangerid.append(Competing_pList.get(i)+"");
						  
					}
				String Picture = mangerid.toString(); 
				Brand = txt_competing_brandName.getText().toString();
				Product = edit_competing_productName.getText().toString();
				
				if (isWrite()) {
					
					//提交数据
					getNetData(Member_ID, Brand, Product, Note, Lng, Lat, location, Picture, Manager_ID);
					
				}

				
				break;
			default:
				break;
			}

		}

	}
	
	/**
	 * 异步更新数据
	 */
	public Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				new ClueCustomToast().showToast(CompetingNewAdd.this,
						R.drawable.toast_sucess, "提交成功！");
				competingList.setIsSubmit(1);
				saveData();
				CompetingNewAdd.this.setResult(1);
				CompetingNewAdd.this.finish();
				break;
			case 1:
				//给客户控件赋值
				txt_competing_clientName.setText(arrayList.get(0).getCompany_name());
				Member_ID = arrayList.get(0).getID();//客户编号
				break;
			case 2:
//				//从sd卡里获取图片进行压缩处理//photoPath为完整路径
//            	Bitmap bitmap = new BitmapTools().getBitmap(photoPath, 480, 800);
//            	//对图片进行剪切成100*100后显示
//            	Bitmap bitmap1 = new BitmapTools().cutBitmap(bitmap, 100, 100);
            	Bitmap bitmap1 = Common.getImageThumbnail(newSDpath, 80, 80);
            	if (Competing_Img_List.size() == 1) {
            		image2.setImageBitmap(bitmap1); 
            		
//            		image2.setImageBitmap( BitmapFactory.decodeFile(path,null));    		
				}else if (Competing_Img_List.size() == 2 ) {
					image3.setImageBitmap(bitmap1);
					
				}else {
					image1.setImageBitmap(bitmap1);
				}
    
            	Competing_Img_List.add(newSDpath);
            	
				break;
			case 4:
            	//传图片到服务器
            	
            	new Thread(){
            		public void run() {
            			File file;
                    	String newPath = Constant.SAVE_IMG_PATH + File.separator + getSystemTime() + ".jpg";
                    	try {
                    		file = PhotoHelpDefine.compressImage(photoPath, newPath, MyApplication.photoWidth, MyApplication.photoHeigh);
//        					file = PhotoHelp.compressImage(photoPath, newPath);
        					Message msg = new Message();
        					msg.obj = file;
        					msg.what = 6;
        					mHandler.sendMessage(msg);
        				} catch (Exception e) {
        					// TODO Auto-generated catch block
        					mHandler.sendEmptyMessage(7);
        					e.printStackTrace();
        					return;
        				}
            		};
            	}.start();
            	
            	break;
            case 5:
            	new ClueCustomToast().showToast(CompetingNewAdd.this,
    					R.drawable.toast_defeat, "提交失败！");
            	break;
            case 6:
            	File file = (File) msg.obj;
            	newSDpath = file.getAbsolutePath();
            	setImageToNet(file.getAbsolutePath());
            	break;
            case 7:
            	pDialog.dismiss();
            	showCustomToast("上传失败", false);
            	break;
			default:
				break;
			}
		};
	};
	
	 /**
     * 传入系统相机拍摄的照片的本地地址，返回调整后的照片本地地址。
     * @param pathOfPicture
     * @return
     */
  	public String makePath(String pathOfPicture){
  		
  		BitmapFactory.Options options = new BitmapFactory.Options();
  		options.inPreferredConfig = Bitmap.Config.RGB_565;
  		// 读取图片到Bitmap
  		Bitmap bitmap = BitmapFactory.decodeFile(pathOfPicture, options);
  		// 将Bitmap调整大小到适应800*600
  		int width = bitmap.getWidth();
  		int height = bitmap.getHeight();
  		// 设置想要的大小(1)
  		int newWidth, newHeight;
  		if (height>width) {//判断是横屏拍的照片还是竖屏的。
  			newWidth = 600;
  			newHeight = 800;
  		}else {
  			newWidth = 800;
  			newHeight = 600;
  		}
  		
  		// 计算缩放比例(2)
  		float scaleWidth = ((float) newWidth) / width;
  		float scaleHeight = ((float) newHeight) / height;
  		// 取得想要缩放的matrix参数(3)
  		Matrix matrix = new Matrix();
  		matrix.postScale(scaleWidth, scaleHeight);
  		// 得到新的图片(4)
  		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
  				
  		
  		// 将调整后的Bitmap存入文件。	
  		String path =  Constant.SAVE_IMG_PATH + File.separator
				+ getSystemTime() + ".jpg";;		
  		try {					
  			File file =new File(path);//输出路径  
  			FileOutputStream out = new FileOutputStream(file);//设置输出流 
  			//退出时保存   
          	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  
              out.flush();  
              out.close();  
  	        
              bitmap.recycle();//释放图片内存
      		bitmap = null;
      		System.gc();
  		} catch (FileNotFoundException e1) {
  			e1.printStackTrace();
  		}  
  		catch (IOException e) {
  			e.printStackTrace();
  		}
  	     
  		// 将新文件地址赋值给pathOfPicture		
  		return path;		
  	}
	
	/**
	 * 传图片到服务器
	 */
	private void setImageToNet(String path) {
		//获得系统时间2014-09-20
		SimpleDateFormat  formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date  curDate=new  Date(System.currentTimeMillis());//获取当前时间
		String  time =formatter.format(curDate);

		String companyID = preferences.getInt(Content.COMPS_ID, 1016) + "";
		String modulePage = "Competition";
		String picData = "";
		try {
			picData = Base64Util.encodeBase64File(path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   UpLoadImgSignText upLoadImgSignText = new UpLoadImgSignText(picData, modulePage, FileName, companyID,
			   time + "\n" + location);
	   upLoadImgSignText.setUpLoadResult(CompetingNewAdd.this);
	   new Thread(upLoadImgSignText).start();
	}

	@Override
	public void onUpLoadResult(String FileName, String FilePath,
			boolean isSuccess) {
		// TODO Auto-generated method stub
		

		if (isSuccess) {
			Competing_pList.add(FilePath);
			//图片上传成功
			L.i("------------->:" +   FilePath);
			pDialog.dismiss();
			new ClueCustomToast().showToast(CompetingNewAdd.this,
					R.drawable.toast_sucess, "图片上传成功！");
			mHandler.sendEmptyMessage(2);
			
		}else {
			//图片上传失败
			L.i("------------->:" + "上传失败" );
			new ClueCustomToast().showToast(CompetingNewAdd.this,
					R.drawable.toast_defeat, "图片上传失败！");
			pDialog.dismiss();
		}
	}
	
	/**
	 * 对返回数据处理
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		L.i("resultCode=="+resultCode+"requestCode=="+requestCode);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CallSystemApp.EXTRA_TAKE_PHOTOS://拍照返回
				L.d("拍照返回");
				FileName = new File(photoPath).getName();
				mHandler.sendEmptyMessage(4);
				//提示图片开始上传
			    pDialog = ProgressDialog.show(this, "请等待...", "正在上传图片...",true);  
			    pDialog.setCancelable(true);
				break;
				
			case 1:
            	//重新调用相机拍照
         		FileName = getSystemTime() + ".jpg";
         		photoPath = Constant.SAVE_IMG_PATH + File.separator
         				+ FileName;
         		CallSystemApp.callCamera(CompetingNewAdd.this, photoPath);
         		
			    break;
				
			case 4://选择客户返回			
				System.out.println("resultCode=="+resultCode+"requestCode=="+requestCode);
		        Bundle bundle = data.getBundleExtra("bundle");//得到新Activity 关闭后返回的数据
		        arrayList = (ArrayList<SortModel>) bundle.getSerializable("result");//接收泛型
		        if (arrayList.size()!=0) {
		        	 mHandler.sendEmptyMessage(1);
				}	        
				break;
			
			default:
				break;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 定位监听
	 */
	private BDLocationListener mLocationListener = new BDLocationListener() {
		
		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			
		     Lng =  (float) location.getLatitude();//经度
			 Lat = (float) location.getLongitude();//维度
			 LocationUtil.stopLocation();
			LocationUtil.startReverseGeoCode(Lng, Lat, getGeoCoderResultListener);
			
		}
	};
	
	OnGetGeoCoderResultListener getGeoCoderResultListener = new OnGetGeoCoderResultListener() {
		
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			// TODO Auto-generated method stub
			if (result == null) {
				return;
			}
			location = result.getAddress();//地理位置
			if (location == null || location.equals("null")) {
				location = "";
			}
			LocationUtil.stopReverseGeoCode();
		}
		
		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * activity从暂停后重新启动改变拍照图片显示
	 */
	protected void onResume() {
		super.onResume();
		// 所有ImageView重置
				image1.setImageResource(R.drawable.visit_img);
				image2.setImageResource(R.drawable.visit_img);
				image3.setImageResource(R.drawable.visit_img);
				
				if (Competing_Img_List.size() >= 1) {
					//从sd卡里获取图片进行压缩处理
			    	Bitmap bitmap = new BitmapTools().getBitmap(Competing_Img_List.get(0), 480, 800);
			    	//对图片进行剪切成100*100后显示
			    	Bitmap bitmap1 = new BitmapTools().cutBitmap(bitmap, 100, 100);
					image1.setImageBitmap(bitmap1);
					if (Competing_Img_List.size() >= 2) {
						//从sd卡里获取图片进行压缩处理
				    	Bitmap bitmap2 = new BitmapTools().getBitmap(Competing_Img_List.get(1), 480, 800);
				    	//对图片进行剪切成100*100后显示
				    	Bitmap bitmap3 = new BitmapTools().cutBitmap(bitmap2, 100, 100);
						image2.setImageBitmap(bitmap3);
						if (Competing_Img_List.size() >= 3) {	
							//从sd卡里获取图片进行压缩处理
					    	Bitmap bitmap4 = new BitmapTools().getBitmap(Competing_Img_List.get(1), 480, 800);
					    	//对图片进行剪切成100*100后显示
					    	Bitmap bitmap5 = new BitmapTools().cutBitmap(bitmap4, 100, 100);
							image3.setImageBitmap(bitmap5);
						}
					}
				}
		
		
	};
	
	/**
	 * 判断是否输入完成
	 * @return 
	 */
	private boolean isWrite(){
		if (txt_competing_clientName.getText().toString().equals("选择客户")) {
			showDialog("请选择客户！");
			return false;
		}
		if (txt_competing_brandName.getText().toString().equals("")) {
			showDialog("请填写品牌！");
			return false;
		}
		if (edit_competing_productName.getText().toString().equals("")) {
			showDialog("本品不能为空！");
			return false;
		}
		if (Competing_Img_List.size()<=0) {
			showDialog("您至少拍一张照片！");
			return false;
		}
		if (Competing_Img_List.size()<=0) {
			showDialog("您至少拍一张照片！");
			return false;
		}
		if (edit_competing_content.getText().toString().equals("")) {
			showDialog("描述不能为空！");
			return false;
		}
		
		return true;
		
		
	}
	
	public void showDialog(String TXT){
		new ClueCustomToast().showToast(CompetingNewAdd.this,
				R.drawable.toast_defeat, TXT);

	}
	
	
	/**
	 * 数据保存
	 */
	private void saveData(){
		//品牌
		competingList.setBrand(txt_competing_brandName.getText().toString().trim());
		//本品
		competingList.setMyBrand(edit_competing_productName.getText().toString().trim());
		//服务器地址
		competingList.setUpUrl(getConnectString(Competing_pList));
    	//拍照后图片绝对路径
		Competing_Img_List.add(photoPath);
    	//本地图片绝对路径拼接
    	competingList.setPhotoUrl(getConnectString(Competing_Img_List));
    	//客户名称
    	competingList.setCustormerName(txt_competing_clientName.getText().toString().trim());
    	 //客户id
    	competingList.setCustormerID(Member_ID);
    	//描述
    	competingList.setNote(edit_competing_content.getText().toString().trim());
    	//保存或者更新数据
		long i = CompetingManager.getInstance(context).saveVisitData(competingList);
		if (i>0) {
			L.e("数据保存成功！");
		}else {
			L.e("数据保存失败！");
		}
		
	}
	
	/**
	 * 图片地址拼接
	 * @return
	 */
	private String getConnectString(List<String> list){
		 //图片地址拼接
		 StringBuffer mangerid = new StringBuffer("");
		 L.e("list.size:" + list.size());
			for (int i = 0; i < list.size(); i++) {
				 L.e("list:" + list.get(i));
				 
				if (i>0) {
					mangerid.append("#");
				}
				mangerid.append(list.get(i)+"");
				  
			}
		String Picture = mangerid.toString().trim(); 
		return Picture;
	}
	
	/**
	 * activity销毁前要进行数据保存
	 * 防止系统回收内存
	 */
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//是否提交
		if (competingList.getIsSubmit() == 0 || competingList.getIsSubmit()==null) {
			competingList.setIsSubmit(0);
		}else {
			competingList.setIsSubmit(1);
		}
        L.e("销毁activity,释放内存");
		saveData();
		super.onDestroy();
		
	}
	
	/**
	 * 按物理按键返回前数据保存
	 */
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//是否提交
    	competingList.setIsSubmit(0);
		saveData();
		super.onBackPressed();
	}
	
	/**
	 * 显示图片
	 */
	@SuppressWarnings("null")
	private void showImage() {
		List<CompetingHostoryList> list = new ArrayList<CompetingHostoryList>();
		list = CompetingManager.getInstance(context).getVisitData(ManagerID);
		Bitmap bitmap = null;
	    String[] temper = null;
		String[] temper1 = null;
		if (list != null && list.size()>0) {
			if (list.get(0).getIsSubmit() == 0) {
			
			txt_competing_clientName.setText(list.get(0).getCustormerName());
			Member_ID = list.get(0).getCustormerID();
			edit_competing_content.setText(list.get(0).getNote());
			txt_competing_brandName.setText(list.get(0).getBrand());
			edit_competing_productName.setText(list.get(0).getMyBrand());
			L.e("显示数据" + list.get(0).getPhotoUrl());
			L.e("显示数据" + list.get(0).getBrand());
			L.e("显示数据" + list.get(0).getMyBrand());
			if (list.get(0).getPhotoUrl() != null && !(list.get(0).getPhotoUrl()).equals("") 
					&& list.get(0).getUpUrl() != null && !(list.get(0).getUpUrl()).equals("")) {
				temper = (list.get(0).getUpUrl()).split("#");
				temper1 = (list.get(0).getPhotoUrl()).split("#");
				if ( temper.length >= 1) {
					bitmap = Common.getImageThumbnail(temper1[0], 80, 80);
					image1.setImageBitmap(bitmap);
					Competing_Img_List.add(temper1[0]);
					Competing_pList.add(temper[0]);
				}
				if ( temper.length >= 2 ) {
					bitmap = Common.getImageThumbnail(temper1[1], 80, 80);
					image2.setImageBitmap(bitmap);
					Competing_Img_List.add(temper1[1]);
					Competing_pList.add(temper[1]);
				}
				if (temper.length >= 3) {
				    bitmap = Common.getImageThumbnail(temper1[2], 80, 80);
					image3.setImageBitmap(bitmap);
					Competing_Img_List.add(temper1[2]);
					Competing_pList.add(temper[2]);
				}
			}
			}
			
		}
		
	}
	
	
}