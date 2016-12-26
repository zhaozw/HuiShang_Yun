package com.huishangyun.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gotye.api.GotyeUser;
import com.huishangyun.Activity.MainActivity;
import com.huishangyun.Adapter.ContactListAcapter;
import com.huishangyun.Util.HanderUtil;
import com.huishangyun.Util.L;
import com.huishangyun.Util.PinyinComparator;
import com.huishangyun.Util.PinyinUtil;
import com.huishangyun.View.SideBar;
import com.huishangyun.manager.MemberManager;
import com.huishangyun.model.Constant;
import com.huishangyun.model.ContactBean;
import com.huishangyun.model.MemberGroups;
import com.huishangyun.model.Members;
import com.huishangyun.Activity.Chat;
import com.huishangyun.App.MyApplication;
import com.huishangyun.yun.R;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 客户列表
 * @author Administrator
 *
 */
public class CustFragment extends Fragment{
	private View mView;
	private EditText searchText;
	private ListView mListView;
	private SideBar mSideBar;
	private TextView textView;
	private ContactListAcapter adapter;
	private ContactListAcapter searchAcapter;
	private List<ContactBean> mList = new ArrayList<ContactBean>();
	private List<ContactBean> searchBeans = new ArrayList<ContactBean>();
	private List<Members> members = new ArrayList<Members>();
	private List<MemberGroups> memberGroups = new ArrayList<MemberGroups>();
	private int ID = 0;
	private int ParentID = 0;
	private ListView serachListView;
	private boolean isSearch = false;
	private MemberManager memberManager;
	private ImageButton backButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub  
		mView = inflater.inflate(R.layout.contact_pager2, container, false);
		initView();
		return mView;
	}
	
	private void initView() {
		searchText = (EditText) mView.findViewById(R.id.et_search_keyword);
		mListView = (ListView) mView.findViewById(R.id.listview_contact_group);
		mSideBar = (SideBar) mView.findViewById(R.id.fast_ke_scroller);
		textView = (TextView) mView.findViewById(R.id.fast_ke_position);
		backButton = (ImageButton) mView.findViewById(R.id.contact_ke_back);
		backButton.setOnClickListener(listener);
		adapter = new ContactListAcapter(getActivity(), mList, 1);
		serachListView = (ListView) mView.findViewById(R.id.searchcontact_kehu);
		searchAcapter = new ContactListAcapter(getActivity(), searchBeans, 1);
		serachListView.setAdapter(searchAcapter);
		serachListView.setVisibility(View.GONE);
		mListView.setAdapter(adapter);
		mSideBar.setTextView(textView);
		mSideBar.setListView(mListView);
		memberManager = MemberManager.getInstance(getActivity());
		mListView.setOnItemClickListener(oClickListener);
		serachListView.setOnItemClickListener(oClickListener);
		searchText.addTextChangedListener(mTextWatcher);
		searchText.clearFocus();
		getContacts(false, 0);
	}
	
	
	/**
	 * 搜索监听
	 */
	private TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			if (s.length() > 0) {
				isSearch = true;
				serachListView.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				new Thread(new SearchContact(s.toString())).start();
				searchText.clearFocus();
			} else {
				isSearch = false;
				serachListView.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
				searchText.clearFocus();
			}
		}
	};
	
	
	private class SearchContact implements Runnable {
		private String keyword;
		public SearchContact (String keyword) {
			this.keyword = keyword;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<Members> list = memberManager.searchMembers(keyword);
			Message msg = new Message();
			msg.what = HanderUtil.case2;
			msg.obj = list;
			mHandler.sendMessage(msg);
		}
		
	}
	
	
	/**
	 * listview点击事件监听
	 */
	private OnItemClickListener oClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if (arg0.getId() ==  R.id.listview_contact_group) {
				((MainActivity)getActivity()).closeInput();
				ContactBean bean = mList.get(arg2);
				if (bean.isIsgroup()) {
					ParentID = bean.getID();
					getContacts(false, bean.getID());
				} else {
					//createUserChat(bean.getJID(), 1, bean.getName(), 0, bean.getSignature());
					OnClicked(bean);
				}

			} else if (arg0.getId() ==  R.id.searchcontact_kehu) {
				((MainActivity)getActivity()).closeInput();
				ContactBean bean = searchBeans.get(arg2);
				if (bean.isIsgroup()) {
					ParentID = bean.getID();
					getContacts(false, bean.getID());
				} else {
					//createUserChat(bean.getJID(), 1, bean.getName(), 0, bean.getSignature());
					OnClicked(bean);
				}
			}
		}
	};
	
	
	LinearLayout CallPhone;
	LinearLayout SendMessage;
	LinearLayout SendChat;
	PopupWindow mPopupWindow;
	
	private void OnClicked(ContactBean cBean){
		
		View mpopView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_menu, null, false);
		mPopupWindow = new PopupWindow(mpopView,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		CallPhone = (LinearLayout)mpopView.findViewById(R.id.menu_call_phone);
		SendMessage = (LinearLayout)mpopView.findViewById(R.id.menu_send_message);
		SendChat = (LinearLayout)mpopView.findViewById(R.id.menu_send_chat);
		CallPhone.setOnClickListener(listener);
		SendMessage.setOnClickListener(listener);
		SendChat.setOnClickListener(listener);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
		mPopupWindow.showAtLocation(((MainActivity)getActivity()).radioGroup, Gravity.BOTTOM, 0, 0);
		mPopupWindow.setAnimationStyle(R.style.app_pop);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.update();
		CallPhone.setTag(cBean.getPhoneNum());
		SendMessage.setTag(cBean.getPhoneNum());
		SendChat.setTag(cBean);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = null; 
			switch (v.getId()) {
			case R.id.menu_call_phone:
				intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + v.getTag().toString())); 
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				//启动
				startActivity(intent);
				mPopupWindow.dismiss();
				break;
			case R.id.menu_send_message:
				intent = new Intent();
				//系统默认的action，用来打开默认的短信界面
				intent.setAction(Intent.ACTION_SENDTO);
				//需要发短息的号码
				intent.setData(Uri.parse("smsto:" + v.getTag().toString()));
				startActivity(intent);
				mPopupWindow.dismiss();
				break;
			case R.id.menu_send_chat:
				ContactBean cbean = (ContactBean) v.getTag();
				createUserChat(cbean.getJID(), 2, cbean.getName(), 0, cbean.getSignature());
				mPopupWindow.dismiss();
				break;
			case R.id.contact_ke_back:
				onBackPressed();
				break;
			default:
				mPopupWindow.dismiss();
				break;
			}
		}
	};
	
	/**
	 * 启动聊天
	 * @param JID
	 * @param type
	 * @param name
	 * @param messtype
	 * @param sSign 个性签名
	 */
	private void createUserChat(String JID,int type,String name ,int messtype, String sSign){
		Intent intent = new Intent(getActivity(),Chat.class);
		intent.putExtra("JID", JID);
		intent.putExtra("type", type);
		intent.putExtra("name", name);
		intent.putExtra("messtype", messtype);
		intent.putExtra("chat_name", name);
		intent.putExtra("Sign", sSign);
		GotyeUser gotyeUser = new GotyeUser(JID);
		intent.putExtra("user", gotyeUser);
		startActivity(intent);
	}
	
	/**
	 * 获取联系人列表
	 * @param isParent
	 * @param ID
	 */
	public void getContacts(boolean isParent, int ID) {
		if (mListView != null) {
			new Thread(new GetContacts(isParent, ID)).start();
		}
	}
	
	/**
	 * 返回键调用
	 * @return
	 */
	public boolean onBackPressed() {
		if (isSearch) {
			searchText.setText("");
			return true;
		} else {
			if (memberGroups.size() == 0) {
				if (ParentID == 0) {
					return false;
				} else {
					getContacts(true, ParentID);
					return true;
				}
			} else {
				if (memberGroups.get(0).getParentID() == 0) {
					return false;
				} else {
					getContacts(true, memberGroups.get(0).getParentID());
					return true;
				}
			}
		}
		
	}
	
	/**
	 * 获取联系人列表
	 * @author Pan
	 *
	 */
	private class GetContacts implements Runnable{
		private boolean isParent;
		private int ID;
		
		public GetContacts(boolean isParent, int ID) {
			this.isParent = isParent;
			this.ID = ID;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			memberGroups.clear();
			members.clear();
			memberGroups = memberManager.getGroups(ID, isParent);
			if (MyApplication.preferences.getString(Constant.HUISHANG_TYPE, "").equals("1")) {
				members = memberManager.getMembersForManager(ID, isParent, Integer.parseInt(MyApplication.preferences.getString(Constant.HUISHANGYUN_UID, "")));
			} else {
				members = memberManager.getMembersForDepartment(ID, isParent, MyApplication.preferences.getInt(Constant.HUISHANG_DEPARTMENT_ID, 0));
			}
			L.e("memberGroups = " + memberGroups.size());
			L.e("members = " + members.size());
			List<ContactBean> contactBeans = new ArrayList<ContactBean>();
			for (MemberGroups memberGroup : memberGroups) {
				ContactBean bean = new ContactBean();
				bean.setIsgroup(true);
				bean.setID(memberGroup.getID());
				bean.setJID("");
				bean.setG_ID(memberGroup.getParentID());
				bean.setName(memberGroup.getName());
				String str = PinyinUtil.getAlpha("#");
				bean.setPinyin(str);
				bean.setLetter("组");
				contactBeans.add(bean);
			}
			for (Members member : members) {
				ContactBean bean = new ContactBean();
				bean.setIsgroup(false);
				bean.setID(member.getID());
				bean.setJID(member.getOFUserName());
				bean.setName(member.getContact());
				bean.setG_ID(0);
				String str = PinyinUtil.getAlpha(member.getContact());
				bean.setPinyin(str);
				bean.setLetter(str.charAt(0)+"");
				bean.setPhoneNum(member.getMobile());
				bean.setDepartment(member.getRealName());
				bean.setPhoneNum(member.getMobile());
				bean.setIsKehu(1);
				bean.setPhoto(member.getPhoto());
				contactBeans.add(bean);
			}
			Message msg = new Message();
			msg.what = HanderUtil.case1;
			msg.obj = contactBeans;
			mHandler.sendMessage(msg);
		}
		
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HanderUtil.case1:
				if (adapter != null) {//刷新数据
					mList.clear();
					List<ContactBean> contactBeans = (List<ContactBean>) msg.obj;
					L.e("contactBeans = " + contactBeans.size());
					for (ContactBean contactBean : contactBeans) {
						mList.add(contactBean);
					}
					Collections.sort(mList, new PinyinComparator());
					adapter.notifyDataSetChanged();
					searchText.clearFocus();
				}
				break;

			case HanderUtil.case2:
				searchBeans.clear();
				for (Members member : (List<Members>)msg.obj) {
					ContactBean bean = new ContactBean();
					bean.setIsgroup(false);
					bean.setID(member.getID());
					bean.setJID(member.getOFUserName());
					bean.setName(member.getContact());
					bean.setG_ID(0);
					String str = PinyinUtil.getAlpha(member.getContact());
					bean.setPinyin(str);
					bean.setLetter(str.charAt(0)+"");
					bean.setPhoneNum(member.getMobile());
					bean.setDepartment(member.getRealName());
					bean.setPhoneNum(member.getMobile());
					bean.setIsKehu(1);
					bean.setPhoto(member.getPhoto());
					searchBeans.add(bean);
				}
				Collections.sort(searchBeans, new PinyinComparator());
				searchAcapter.notifyDataSetChanged();
				searchText.clearFocus();
				break;
			default:
				break;
			}
		};
	};
	
}