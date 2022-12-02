package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.sql.SQL _clientes = null;
public anywheresoftware.b4a.objects.EditTextWrapper _user = null;
public anywheresoftware.b4a.objects.EditTextWrapper _password = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_login = null;
public anywheresoftware.b4a.objects.TabHostWrapper _tabhost1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_menu_ingr = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_ingresar = null;
public anywheresoftware.b4a.objects.EditTextWrapper _input_patente = null;
public static String _fecha = "";
public static String _hin = "";
public static String _hout = "";
public static int _monto = 0;
public anywheresoftware.b4a.objects.ButtonWrapper _listar = null;
public anywheresoftware.b4a.sql.SQL.CursorWrapper _mi_cursor = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel_ingreso = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_confirm = null;
public anywheresoftware.b4a.objects.LabelWrapper _conf_patente = null;
public anywheresoftware.b4a.objects.LabelWrapper _conf_fecha = null;
public anywheresoftware.b4a.objects.LabelWrapper _conf_hora = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_retirar = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_buscar = null;
public anywheresoftware.b4a.objects.LabelWrapper _msg_retiro = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel_retiro = null;
public static boolean _found = false;
public static int _i = 0;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_conf_retiro = null;
public anywheresoftware.b4a.objects.EditTextWrapper _input_patente2 = null;
public static String _patente = "";
public anywheresoftware.b4a.objects.LabelWrapper _tick_patente = null;
public anywheresoftware.b4a.objects.LabelWrapper _tick_fecha = null;
public anywheresoftware.b4a.objects.LabelWrapper _tick_hin = null;
public anywheresoftware.b4a.objects.LabelWrapper _tick_hout = null;
public anywheresoftware.b4a.objects.LabelWrapper _tick_monto = null;
public anywheresoftware.b4a.objects.LabelWrapper _tick_tiempo = null;
public anywheresoftware.b4a.phone.Phone _phone1 = null;
public anywheresoftware.b4a.objects.TabHostWrapper _tab_vistas = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lista_todos = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lista_retirados = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lista_ins = null;
public static int _tot_estacionados = 0;
public static int _tot_retirados = 0;
public anywheresoftware.b4a.objects.LabelWrapper _txt_todos = null;
public anywheresoftware.b4a.objects.LabelWrapper _txt_tot_estacionados = null;
public anywheresoftware.b4a.objects.LabelWrapper _txt_tot_retirados = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_atras = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_error_ing = null;
public anywheresoftware.b4a.objects.LabelWrapper _msg_error = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel_error_ingreso = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_invitado = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bot_exit = null;
public b4a.example.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 78;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 80;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 81;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 83;BA.debugLine="Clientes.Initialize(File.DirInternal,\"Cliente3.d";
_clientes.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"Cliente3.db",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 86;BA.debugLine="Clientes.BeginTransaction";
_clientes.BeginTransaction();
 //BA.debugLineNum = 87;BA.debugLine="Try";
try { //BA.debugLineNum = 88;BA.debugLine="Clientes.ExecNonQuery(\"CREATE TABLE IF NOT EXIS";
_clientes.ExecNonQuery("CREATE TABLE IF NOT EXISTS autos1 (id_dato INTEGER PRIMARY KEY AUTOINCREMENT,patente TEXT, fecha Date, hin TEXT, monto INTEGER, hout TEXT)");
 //BA.debugLineNum = 89;BA.debugLine="Clientes.TransactionSuccessful";
_clientes.TransactionSuccessful();
 //BA.debugLineNum = 90;BA.debugLine="ToastMessageShow(\"Crea la base\",True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Crea la base"),anywheresoftware.b4a.keywords.Common.True);
 } 
       catch (Exception e10) {
			processBA.setLastException(e10); //BA.debugLineNum = 92;BA.debugLine="Log(\"ERROR de Creacion base de datos: \"&LastExc";
anywheresoftware.b4a.keywords.Common.Log("ERROR de Creacion base de datos: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 };
 //BA.debugLineNum = 94;BA.debugLine="Clientes.EndTransaction";
_clientes.EndTransaction();
 };
 //BA.debugLineNum = 96;BA.debugLine="TabHost1.AddTab(\"0\",\"1.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"0","1.bal");
 //BA.debugLineNum = 97;BA.debugLine="TabHost1.AddTab(\"1\",\"2.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"1","2.bal");
 //BA.debugLineNum = 98;BA.debugLine="TabHost1.AddTab(\"2\",\"2_1.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"2","2_1.bal");
 //BA.debugLineNum = 99;BA.debugLine="TabHost1.AddTab(\"3\",\"3.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"3","3.bal");
 //BA.debugLineNum = 100;BA.debugLine="TabHost1.AddTab(\"4\",\"4.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"4","4.bal");
 //BA.debugLineNum = 101;BA.debugLine="TabHost1.AddTab(\"5\",\"4_1.bal\")";
mostCurrent._tabhost1.AddTab(mostCurrent.activityBA,"5","4_1.bal");
 //BA.debugLineNum = 102;BA.debugLine="tab_vistas.AddTab(\"Estacionados\",\"3_1.bal\")";
mostCurrent._tab_vistas.AddTab(mostCurrent.activityBA,"Estacionados","3_1.bal");
 //BA.debugLineNum = 103;BA.debugLine="tab_vistas.AddTab(\"Retirados\",\"3_2.bal\")";
mostCurrent._tab_vistas.AddTab(mostCurrent.activityBA,"Retirados","3_2.bal");
 //BA.debugLineNum = 104;BA.debugLine="tab_vistas.AddTab(\"Todos\",\"3_3.bal\")";
mostCurrent._tab_vistas.AddTab(mostCurrent.activityBA,"Todos","3_3.bal");
 //BA.debugLineNum = 105;BA.debugLine="panel_ingreso.Visible = False";
mostCurrent._panel_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 106;BA.debugLine="panel_retiro.Visible = False";
mostCurrent._panel_retiro.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 107;BA.debugLine="input_patente.Color = Colors.RGB(242, 247, 248)";
mostCurrent._input_patente.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (242),(int) (247),(int) (248)));
 //BA.debugLineNum = 108;BA.debugLine="input_patente2.Color = Colors.RGB(242, 247, 248)";
mostCurrent._input_patente2.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (242),(int) (247),(int) (248)));
 //BA.debugLineNum = 109;BA.debugLine="user.Color = Colors.RGB(242, 247, 248)";
mostCurrent._user.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (242),(int) (247),(int) (248)));
 //BA.debugLineNum = 110;BA.debugLine="password.Color = Colors.RGB(242, 247, 248)";
mostCurrent._password.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (242),(int) (247),(int) (248)));
 //BA.debugLineNum = 111;BA.debugLine="TabHost1.CurrentTab = 1";
mostCurrent._tabhost1.setCurrentTab((int) (1));
 //BA.debugLineNum = 113;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 119;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 121;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 115;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 117;BA.debugLine="End Sub";
return "";
}
public static String  _bot_atras_click() throws Exception{
 //BA.debugLineNum = 356;BA.debugLine="Sub bot_atras_Click";
 //BA.debugLineNum = 357;BA.debugLine="TabHost1.CurrentTab = 1";
mostCurrent._tabhost1.setCurrentTab((int) (1));
 //BA.debugLineNum = 358;BA.debugLine="panel_retiro.Visible = False";
mostCurrent._panel_retiro.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 359;BA.debugLine="panel_ingreso.Visible = False";
mostCurrent._panel_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 360;BA.debugLine="input_patente2.Text = \"\"";
mostCurrent._input_patente2.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 361;BA.debugLine="input_patente.Text = \"\"";
mostCurrent._input_patente.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 362;BA.debugLine="End Sub";
return "";
}
public static String  _bot_buscar_click() throws Exception{
 //BA.debugLineNum = 233;BA.debugLine="Sub bot_buscar_Click";
 //BA.debugLineNum = 234;BA.debugLine="found = False";
_found = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 235;BA.debugLine="i = 0";
_i = (int) (0);
 //BA.debugLineNum = 236;BA.debugLine="If input_patente2.Text <> \"\" And input_patente2.T";
if ((mostCurrent._input_patente2.getText()).equals("") == false && mostCurrent._input_patente2.getText().length()==6) { 
 //BA.debugLineNum = 237;BA.debugLine="Mi_Cursor=Clientes.ExecQuery(\"SELECT * FROM auto";
mostCurrent._mi_cursor.setObject((android.database.Cursor)(_clientes.ExecQuery("SELECT * FROM autos1")));
 //BA.debugLineNum = 238;BA.debugLine="If Mi_Cursor.RowCount>0 Then";
if (mostCurrent._mi_cursor.getRowCount()>0) { 
 //BA.debugLineNum = 241;BA.debugLine="Do While (Not(found) And i<Mi_Cursor.RowCount)";
while ((anywheresoftware.b4a.keywords.Common.Not(_found) && _i<mostCurrent._mi_cursor.getRowCount())) {
 //BA.debugLineNum = 242;BA.debugLine="Mi_Cursor.Position=i";
mostCurrent._mi_cursor.setPosition(_i);
 //BA.debugLineNum = 243;BA.debugLine="If Mi_Cursor.GetString(\"patente\") == input_pat";
if ((mostCurrent._mi_cursor.GetString("patente")).equals(mostCurrent._input_patente2.getText().toUpperCase()) && (mostCurrent._mi_cursor.GetString("hout")).equals("-")) { 
 //BA.debugLineNum = 244;BA.debugLine="patente = Mi_Cursor.GetString(\"patente\")";
mostCurrent._patente = mostCurrent._mi_cursor.GetString("patente");
 //BA.debugLineNum = 245;BA.debugLine="found = True";
_found = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 246;BA.debugLine="Log(\"patente encontrada\")";
anywheresoftware.b4a.keywords.Common.Log("patente encontrada");
 };
 //BA.debugLineNum = 248;BA.debugLine="i = i+1";
_i = (int) (_i+1);
 }
;
 };
 };
 //BA.debugLineNum = 252;BA.debugLine="bot_buscar.Visible = False";
mostCurrent._bot_buscar.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 253;BA.debugLine="panel_retiro.Visible = True";
mostCurrent._panel_retiro.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 254;BA.debugLine="If found Then";
if (_found) { 
 //BA.debugLineNum = 255;BA.debugLine="msg_retiro.Text = \"Pantente encontrada\"";
mostCurrent._msg_retiro.setText(BA.ObjectToCharSequence("Pantente encontrada"));
 //BA.debugLineNum = 256;BA.debugLine="bot_conf_retiro.Text = \"Generar Ticket\"";
mostCurrent._bot_conf_retiro.setText(BA.ObjectToCharSequence("Generar Ticket"));
 }else if(mostCurrent._input_patente2.getText().length()==6 && anywheresoftware.b4a.keywords.Common.Not(_found)) { 
 //BA.debugLineNum = 258;BA.debugLine="msg_retiro.Text = \"Patente no encontrada\"";
mostCurrent._msg_retiro.setText(BA.ObjectToCharSequence("Patente no encontrada"));
 //BA.debugLineNum = 259;BA.debugLine="bot_conf_retiro.Text = \"Reintentar\"";
mostCurrent._bot_conf_retiro.setText(BA.ObjectToCharSequence("Reintentar"));
 }else {
 //BA.debugLineNum = 261;BA.debugLine="msg_retiro.Text = \"Patente no válida, debe tener";
mostCurrent._msg_retiro.setText(BA.ObjectToCharSequence("Patente no válida, debe tener 6 caracteres"));
 //BA.debugLineNum = 262;BA.debugLine="bot_conf_retiro.Text = \"Reintentar\"";
mostCurrent._bot_conf_retiro.setText(BA.ObjectToCharSequence("Reintentar"));
 };
 //BA.debugLineNum = 264;BA.debugLine="phone1.HideKeyboard(Activity)";
mostCurrent._phone1.HideKeyboard(mostCurrent._activity);
 //BA.debugLineNum = 265;BA.debugLine="End Sub";
return "";
}
public static String  _bot_conf_retiro_click() throws Exception{
 //BA.debugLineNum = 267;BA.debugLine="Sub bot_conf_retiro_Click";
 //BA.debugLineNum = 268;BA.debugLine="panel_retiro.Visible = False";
mostCurrent._panel_retiro.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 269;BA.debugLine="If found Then";
if (_found) { 
 //BA.debugLineNum = 270;BA.debugLine="update_values(i)";
_update_values(_i);
 //BA.debugLineNum = 271;BA.debugLine="gen_ticket(i)";
_gen_ticket(_i);
 //BA.debugLineNum = 272;BA.debugLine="ToastMessageShow(\"Vehiculo retirado\",False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Vehiculo retirado"),anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 273;BA.debugLine="TabHost1.CurrentTab = 5";
mostCurrent._tabhost1.setCurrentTab((int) (5));
 }else {
 //BA.debugLineNum = 275;BA.debugLine="input_patente2.Text = \"\"";
mostCurrent._input_patente2.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 276;BA.debugLine="bot_buscar.Visible = True";
mostCurrent._bot_buscar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 278;BA.debugLine="End Sub";
return "";
}
public static String  _bot_confirm_click() throws Exception{
 //BA.debugLineNum = 204;BA.debugLine="Sub bot_confirm_Click";
 //BA.debugLineNum = 205;BA.debugLine="DateTime.DateFormat = \"HH:mm\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("HH:mm");
 //BA.debugLineNum = 206;BA.debugLine="hin = DateTime.Date(DateTime.Now)";
mostCurrent._hin = anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow());
 //BA.debugLineNum = 207;BA.debugLine="Log(hin)";
anywheresoftware.b4a.keywords.Common.Log(mostCurrent._hin);
 //BA.debugLineNum = 208;BA.debugLine="DateTime.DateFormat = \"dd-MM-yyyy\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("dd-MM-yyyy");
 //BA.debugLineNum = 209;BA.debugLine="fecha = DateTime.Date(DateTime.Now)";
mostCurrent._fecha = anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow());
 //BA.debugLineNum = 210;BA.debugLine="Clientes.BeginTransaction";
_clientes.BeginTransaction();
 //BA.debugLineNum = 211;BA.debugLine="Try";
try { //BA.debugLineNum = 212;BA.debugLine="Clientes.ExecNonQuery2(\"INSERT INTO autos1 (pate";
_clientes.ExecNonQuery2("INSERT INTO autos1 (patente, fecha, hin, monto, hout) VALUES (?,?,?,?,?)",anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{mostCurrent._patente.toUpperCase(),mostCurrent._fecha,mostCurrent._hin,BA.NumberToString(0),"-"}));
 //BA.debugLineNum = 213;BA.debugLine="Clientes.TransactionSuccessful";
_clientes.TransactionSuccessful();
 //BA.debugLineNum = 214;BA.debugLine="Log(\"auto ingresado\")";
anywheresoftware.b4a.keywords.Common.Log("auto ingresado");
 //BA.debugLineNum = 215;BA.debugLine="ToastMessageShow(\"Vehiculo ingresado\",False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Vehiculo ingresado"),anywheresoftware.b4a.keywords.Common.False);
 } 
       catch (Exception e13) {
			processBA.setLastException(e13); //BA.debugLineNum = 217;BA.debugLine="Log(\"catch: \" & LastException.Message)";
anywheresoftware.b4a.keywords.Common.Log("catch: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 };
 //BA.debugLineNum = 219;BA.debugLine="Clientes.EndTransaction";
_clientes.EndTransaction();
 //BA.debugLineNum = 220;BA.debugLine="input_patente2.Text = \"\"";
mostCurrent._input_patente2.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 221;BA.debugLine="panel_ingreso.Visible = False";
mostCurrent._panel_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 222;BA.debugLine="bot_ingresar.Visible = True";
mostCurrent._bot_ingresar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 223;BA.debugLine="TabHost1.CurrentTab = 1";
mostCurrent._tabhost1.setCurrentTab((int) (1));
 //BA.debugLineNum = 224;BA.debugLine="phone1.HideKeyboard(Activity)";
mostCurrent._phone1.HideKeyboard(mostCurrent._activity);
 //BA.debugLineNum = 225;BA.debugLine="End Sub";
return "";
}
public static String  _bot_error_ing_click() throws Exception{
 //BA.debugLineNum = 364;BA.debugLine="Sub bot_error_ing_Click";
 //BA.debugLineNum = 365;BA.debugLine="phone1.HideKeyboard(Activity)";
mostCurrent._phone1.HideKeyboard(mostCurrent._activity);
 //BA.debugLineNum = 366;BA.debugLine="panel_error_ingreso.Visible = False";
mostCurrent._panel_error_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 367;BA.debugLine="input_patente.Text = \"\"";
mostCurrent._input_patente.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 368;BA.debugLine="bot_ingresar.Visible = True";
mostCurrent._bot_ingresar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 369;BA.debugLine="End Sub";
return "";
}
public static String  _bot_exit_click() throws Exception{
 //BA.debugLineNum = 378;BA.debugLine="Sub bot_exit_Click";
 //BA.debugLineNum = 379;BA.debugLine="TabHost1.CurrentTab = 0";
mostCurrent._tabhost1.setCurrentTab((int) (0));
 //BA.debugLineNum = 380;BA.debugLine="user.Text = \"\"";
mostCurrent._user.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 381;BA.debugLine="password.Text = \"\"";
mostCurrent._password.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 382;BA.debugLine="End Sub";
return "";
}
public static String  _bot_ingresar_click() throws Exception{
 //BA.debugLineNum = 141;BA.debugLine="Sub bot_ingresar_Click";
 //BA.debugLineNum = 142;BA.debugLine="If input_patente.Text.Length == 6 Then";
if (mostCurrent._input_patente.getText().length()==6) { 
 //BA.debugLineNum = 143;BA.debugLine="patente = input_patente.Text";
mostCurrent._patente = mostCurrent._input_patente.getText();
 //BA.debugLineNum = 144;BA.debugLine="panel_ingreso.Visible = True";
mostCurrent._panel_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 145;BA.debugLine="bot_ingresar.Visible = False";
mostCurrent._bot_ingresar.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 146;BA.debugLine="conf_patente.Text = \"Patente: \" & input_patente.";
mostCurrent._conf_patente.setText(BA.ObjectToCharSequence("Patente: "+mostCurrent._input_patente.getText().toUpperCase()));
 //BA.debugLineNum = 147;BA.debugLine="DateTime.DateFormat = \"HH:mm\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("HH:mm");
 //BA.debugLineNum = 148;BA.debugLine="conf_hora.Text = \"Hora ingreso: \" & DateTime.Dat";
mostCurrent._conf_hora.setText(BA.ObjectToCharSequence("Hora ingreso: "+anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 149;BA.debugLine="DateTime.DateFormat = \"dd-MM-yyyy\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("dd-MM-yyyy");
 //BA.debugLineNum = 150;BA.debugLine="conf_fecha.Text = \"Fecha: \" & DateTime.Date(Date";
mostCurrent._conf_fecha.setText(BA.ObjectToCharSequence("Fecha: "+anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow())));
 //BA.debugLineNum = 151;BA.debugLine="phone1.HideKeyboard(Activity)";
mostCurrent._phone1.HideKeyboard(mostCurrent._activity);
 }else {
 //BA.debugLineNum = 153;BA.debugLine="panel_error_ingreso.Visible = True";
mostCurrent._panel_error_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 154;BA.debugLine="msg_error.Text = \"Patente no válida, debe tener";
mostCurrent._msg_error.setText(BA.ObjectToCharSequence("Patente no válida, debe tener 6 caracteres"));
 //BA.debugLineNum = 155;BA.debugLine="bot_error_ing.Text = \"Reintentar\"";
mostCurrent._bot_error_ing.setText(BA.ObjectToCharSequence("Reintentar"));
 //BA.debugLineNum = 156;BA.debugLine="bot_ingresar.Visible = False";
mostCurrent._bot_ingresar.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 158;BA.debugLine="input_patente.Text = \"\"";
mostCurrent._input_patente.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 159;BA.debugLine="End Sub";
return "";
}
public static String  _bot_invitado_click() throws Exception{
 //BA.debugLineNum = 371;BA.debugLine="Sub bot_invitado_Click";
 //BA.debugLineNum = 372;BA.debugLine="TabHost1.CurrentTab = 1";
mostCurrent._tabhost1.setCurrentTab((int) (1));
 //BA.debugLineNum = 373;BA.debugLine="bot_menu_ingr.Visible = False";
mostCurrent._bot_menu_ingr.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 374;BA.debugLine="bot_retirar.Visible = False";
mostCurrent._bot_retirar.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 375;BA.debugLine="listar.Top = 340dip";
mostCurrent._listar.setTop(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (340)));
 //BA.debugLineNum = 376;BA.debugLine="End Sub";
return "";
}
public static String  _bot_login_click() throws Exception{
 //BA.debugLineNum = 124;BA.debugLine="Sub bot_login_Click";
 //BA.debugLineNum = 125;BA.debugLine="bot_menu_ingr.Visible = True";
mostCurrent._bot_menu_ingr.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 126;BA.debugLine="bot_retirar.Visible = True";
mostCurrent._bot_retirar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 129;BA.debugLine="Log(\"logged in\")";
anywheresoftware.b4a.keywords.Common.Log("logged in");
 //BA.debugLineNum = 130;BA.debugLine="TabHost1.CurrentTab = 1";
mostCurrent._tabhost1.setCurrentTab((int) (1));
 //BA.debugLineNum = 131;BA.debugLine="phone1.HideKeyboard(Activity)";
mostCurrent._phone1.HideKeyboard(mostCurrent._activity);
 //BA.debugLineNum = 133;BA.debugLine="End Sub";
return "";
}
public static String  _bot_menu_ingr_click() throws Exception{
 //BA.debugLineNum = 135;BA.debugLine="Sub bot_menu_ingr_Click";
 //BA.debugLineNum = 136;BA.debugLine="TabHost1.CurrentTab = 2";
mostCurrent._tabhost1.setCurrentTab((int) (2));
 //BA.debugLineNum = 137;BA.debugLine="bot_ingresar.Visible = True";
mostCurrent._bot_ingresar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 138;BA.debugLine="panel_error_ingreso.Visible = False";
mostCurrent._panel_error_ingreso.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 139;BA.debugLine="End Sub";
return "";
}
public static String  _bot_retirar_click() throws Exception{
 //BA.debugLineNum = 227;BA.debugLine="Sub bot_retirar_Click";
 //BA.debugLineNum = 228;BA.debugLine="found = False";
_found = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 229;BA.debugLine="bot_buscar.Visible = True";
mostCurrent._bot_buscar.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 230;BA.debugLine="TabHost1.CurrentTab = 4";
mostCurrent._tabhost1.setCurrentTab((int) (4));
 //BA.debugLineNum = 231;BA.debugLine="End Sub";
return "";
}
public static int  _calc_monto(String _tiempo_tot) throws Exception{
int _minutos = 0;
int _total = 0;
 //BA.debugLineNum = 280;BA.debugLine="Sub calc_monto(tiempo_tot As String) As Int";
 //BA.debugLineNum = 281;BA.debugLine="Log(tiempo_tot)";
anywheresoftware.b4a.keywords.Common.Log(_tiempo_tot);
 //BA.debugLineNum = 282;BA.debugLine="Private minutos As Int = tiempo_tot.SubString2(0,";
_minutos = (int) ((double)(Double.parseDouble(_tiempo_tot.substring((int) (0),(int) (2))))*60+(double)(Double.parseDouble(_tiempo_tot.substring((int) (3),(int) (5)))));
 //BA.debugLineNum = 283;BA.debugLine="Private total As Int";
_total = 0;
 //BA.debugLineNum = 284;BA.debugLine="If minutos < 15 Then";
if (_minutos<15) { 
 //BA.debugLineNum = 285;BA.debugLine="total = 500";
_total = (int) (500);
 }else {
 //BA.debugLineNum = 287;BA.debugLine="minutos = minutos - 15";
_minutos = (int) (_minutos-15);
 //BA.debugLineNum = 288;BA.debugLine="total = 500 + minutos*30";
_total = (int) (500+_minutos*30);
 };
 //BA.debugLineNum = 290;BA.debugLine="Return total";
if (true) return _total;
 //BA.debugLineNum = 291;BA.debugLine="End Sub";
return 0;
}
public static String  _calc_tiempo(String _hora_in,String _hora_sal) throws Exception{
String _dif_horas = "";
String _dif_minutes = "";
String _res = "";
boolean _menor = false;
 //BA.debugLineNum = 325;BA.debugLine="Sub calc_tiempo(hora_in As String, hora_sal As Str";
 //BA.debugLineNum = 326;BA.debugLine="Private dif_horas As String";
_dif_horas = "";
 //BA.debugLineNum = 327;BA.debugLine="Private dif_minutes As String";
_dif_minutes = "";
 //BA.debugLineNum = 328;BA.debugLine="Private res As String";
_res = "";
 //BA.debugLineNum = 329;BA.debugLine="Private menor As Boolean = False";
_menor = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 330;BA.debugLine="Log(\"hsal \"&hora_sal&\"   hin \"&hora_in)";
anywheresoftware.b4a.keywords.Common.Log("hsal "+_hora_sal+"   hin "+_hora_in);
 //BA.debugLineNum = 331;BA.debugLine="If hora_sal.SubString2(3,4) >= hora_in.SubString2";
if ((double)(Double.parseDouble(_hora_sal.substring((int) (3),(int) (4))))>=(double)(Double.parseDouble(_hora_in.substring((int) (3),(int) (4))))) { 
 //BA.debugLineNum = 332;BA.debugLine="dif_minutes = hora_sal.SubString2(3,5) - hora_in";
_dif_minutes = BA.NumberToString((double)(Double.parseDouble(_hora_sal.substring((int) (3),(int) (5))))-(double)(Double.parseDouble(_hora_in.substring((int) (3),(int) (5)))));
 }else {
 //BA.debugLineNum = 334;BA.debugLine="dif_minutes = hora_sal.SubString2(3,5) - hora_in";
_dif_minutes = BA.NumberToString((double)(Double.parseDouble(_hora_sal.substring((int) (3),(int) (5))))-(double)(Double.parseDouble(_hora_in.substring((int) (3),(int) (5))))+60);
 //BA.debugLineNum = 335;BA.debugLine="menor = True";
_menor = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 337;BA.debugLine="If hora_sal.SubString2(0,2) < hora_in.SubString2(";
if ((double)(Double.parseDouble(_hora_sal.substring((int) (0),(int) (2))))<(double)(Double.parseDouble(_hora_in.substring((int) (0),(int) (2))))) { 
 //BA.debugLineNum = 338;BA.debugLine="dif_horas = hora_sal.SubString2(0,2) - hora_in.S";
_dif_horas = BA.NumberToString((double)(Double.parseDouble(_hora_sal.substring((int) (0),(int) (2))))-(double)(Double.parseDouble(_hora_in.substring((int) (0),(int) (2))))+24);
 }else if(_menor) { 
 //BA.debugLineNum = 340;BA.debugLine="dif_horas = hora_sal.SubString2(0,2) - hora_in.S";
_dif_horas = BA.NumberToString((double)(Double.parseDouble(_hora_sal.substring((int) (0),(int) (2))))-(double)(Double.parseDouble(_hora_in.substring((int) (0),(int) (2))))-1);
 }else {
 //BA.debugLineNum = 342;BA.debugLine="dif_horas = hora_sal.SubString2(0,2) - hora_in.S";
_dif_horas = BA.NumberToString((double)(Double.parseDouble(_hora_sal.substring((int) (0),(int) (2))))-(double)(Double.parseDouble(_hora_in.substring((int) (0),(int) (2)))));
 };
 //BA.debugLineNum = 345;BA.debugLine="If dif_horas < 10 Then";
if ((double)(Double.parseDouble(_dif_horas))<10) { 
 //BA.debugLineNum = 346;BA.debugLine="dif_horas = \"0\" & dif_horas";
_dif_horas = "0"+_dif_horas;
 };
 //BA.debugLineNum = 348;BA.debugLine="If dif_minutes < 10 Then";
if ((double)(Double.parseDouble(_dif_minutes))<10) { 
 //BA.debugLineNum = 349;BA.debugLine="dif_minutes = \"0\" & dif_minutes";
_dif_minutes = "0"+_dif_minutes;
 };
 //BA.debugLineNum = 351;BA.debugLine="res = dif_horas&\":\"&dif_minutes";
_res = _dif_horas+":"+_dif_minutes;
 //BA.debugLineNum = 352;BA.debugLine="Return  res";
if (true) return _res;
 //BA.debugLineNum = 353;BA.debugLine="End Sub";
return "";
}
public static String  _gen_ticket(int _idx) throws Exception{
 //BA.debugLineNum = 293;BA.debugLine="Sub gen_ticket(idx As Int)";
 //BA.debugLineNum = 294;BA.debugLine="Clientes.BeginTransaction";
_clientes.BeginTransaction();
 //BA.debugLineNum = 295;BA.debugLine="Mi_Cursor=Clientes.ExecQuery(\"SELECT * FROM autos";
mostCurrent._mi_cursor.setObject((android.database.Cursor)(_clientes.ExecQuery("SELECT * FROM autos1")));
 //BA.debugLineNum = 296;BA.debugLine="Mi_Cursor.Position = idx - 1";
mostCurrent._mi_cursor.setPosition((int) (_idx-1));
 //BA.debugLineNum = 297;BA.debugLine="tick_patente.Text = \"PATENTE: \" & Mi_Cursor.GetSt";
mostCurrent._tick_patente.setText(BA.ObjectToCharSequence("PATENTE: "+mostCurrent._mi_cursor.GetString("patente")));
 //BA.debugLineNum = 298;BA.debugLine="tick_fecha.Text = \"FECHA: \" & Mi_Cursor.GetString";
mostCurrent._tick_fecha.setText(BA.ObjectToCharSequence("FECHA: "+mostCurrent._mi_cursor.GetString("fecha")));
 //BA.debugLineNum = 299;BA.debugLine="tick_hin.Text = \"HORA INGRESO: \" & Mi_Cursor.GetS";
mostCurrent._tick_hin.setText(BA.ObjectToCharSequence("HORA INGRESO: "+mostCurrent._mi_cursor.GetString("hin")));
 //BA.debugLineNum = 300;BA.debugLine="tick_hout.Text = \"HORA SALIDA: \" & Mi_Cursor.GetS";
mostCurrent._tick_hout.setText(BA.ObjectToCharSequence("HORA SALIDA: "+mostCurrent._mi_cursor.GetString("hout")));
 //BA.debugLineNum = 301;BA.debugLine="tick_monto.Text = \"MONTO: $\" & Mi_Cursor.GetInt(\"";
mostCurrent._tick_monto.setText(BA.ObjectToCharSequence("MONTO: $"+BA.NumberToString(mostCurrent._mi_cursor.GetInt("monto"))));
 //BA.debugLineNum = 302;BA.debugLine="Log(\"ra \"& Mi_Cursor.GetString(\"monto\"))";
anywheresoftware.b4a.keywords.Common.Log("ra "+mostCurrent._mi_cursor.GetString("monto"));
 //BA.debugLineNum = 303;BA.debugLine="tick_tiempo.Text = \"TIEMPO TOTAL: \" & calc_tiempo";
mostCurrent._tick_tiempo.setText(BA.ObjectToCharSequence("TIEMPO TOTAL: "+_calc_tiempo(mostCurrent._mi_cursor.GetString("hin"),mostCurrent._mi_cursor.GetString("hout"))));
 //BA.debugLineNum = 305;BA.debugLine="Clientes.EndTransaction";
_clientes.EndTransaction();
 //BA.debugLineNum = 306;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 22;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 26;BA.debugLine="Private user As EditText";
mostCurrent._user = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private password As EditText";
mostCurrent._password = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private bot_login As Button";
mostCurrent._bot_login = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private TabHost1 As TabHost";
mostCurrent._tabhost1 = new anywheresoftware.b4a.objects.TabHostWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private bot_menu_ingr As Button";
mostCurrent._bot_menu_ingr = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private bot_ingresar As Button";
mostCurrent._bot_ingresar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private input_patente As EditText";
mostCurrent._input_patente = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Dim fecha As String";
mostCurrent._fecha = "";
 //BA.debugLineNum = 34;BA.debugLine="Dim hin As String";
mostCurrent._hin = "";
 //BA.debugLineNum = 35;BA.debugLine="Dim hout As String";
mostCurrent._hout = "";
 //BA.debugLineNum = 36;BA.debugLine="Dim monto As Int";
_monto = 0;
 //BA.debugLineNum = 37;BA.debugLine="Private listar As Button";
mostCurrent._listar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Dim Mi_Cursor As Cursor";
mostCurrent._mi_cursor = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Private panel_ingreso As Panel";
mostCurrent._panel_ingreso = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Private bot_confirm As Button";
mostCurrent._bot_confirm = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Private conf_patente As Label";
mostCurrent._conf_patente = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Private conf_fecha As Label";
mostCurrent._conf_fecha = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Private conf_hora As Label";
mostCurrent._conf_hora = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 44;BA.debugLine="Private bot_retirar As Button";
mostCurrent._bot_retirar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Private bot_buscar As Button";
mostCurrent._bot_buscar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Private msg_retiro As Label";
mostCurrent._msg_retiro = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Private panel_retiro As Panel";
mostCurrent._panel_retiro = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Dim found As Boolean";
_found = false;
 //BA.debugLineNum = 49;BA.debugLine="Dim i As Int";
_i = 0;
 //BA.debugLineNum = 50;BA.debugLine="Private bot_conf_retiro As Button";
mostCurrent._bot_conf_retiro = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 51;BA.debugLine="Private input_patente2 As EditText";
mostCurrent._input_patente2 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 52;BA.debugLine="Dim patente As String";
mostCurrent._patente = "";
 //BA.debugLineNum = 53;BA.debugLine="Private tick_patente As Label";
mostCurrent._tick_patente = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 54;BA.debugLine="Private tick_fecha As Label";
mostCurrent._tick_fecha = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 55;BA.debugLine="Private tick_hin As Label";
mostCurrent._tick_hin = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 56;BA.debugLine="Private tick_hout As Label";
mostCurrent._tick_hout = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 57;BA.debugLine="Private tick_monto As Label";
mostCurrent._tick_monto = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 58;BA.debugLine="Private tick_tiempo As Label";
mostCurrent._tick_tiempo = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 59;BA.debugLine="Dim patente As String";
mostCurrent._patente = "";
 //BA.debugLineNum = 60;BA.debugLine="Dim phone1 As Phone";
mostCurrent._phone1 = new anywheresoftware.b4a.phone.Phone();
 //BA.debugLineNum = 61;BA.debugLine="Private tab_vistas As TabHost";
mostCurrent._tab_vistas = new anywheresoftware.b4a.objects.TabHostWrapper();
 //BA.debugLineNum = 62;BA.debugLine="Private lista_todos As ListView";
mostCurrent._lista_todos = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 63;BA.debugLine="Private lista_retirados As ListView";
mostCurrent._lista_retirados = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 64;BA.debugLine="Private lista_ins As ListView";
mostCurrent._lista_ins = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 65;BA.debugLine="Dim tot_estacionados As Int";
_tot_estacionados = 0;
 //BA.debugLineNum = 66;BA.debugLine="Dim tot_retirados As Int";
_tot_retirados = 0;
 //BA.debugLineNum = 67;BA.debugLine="Private txt_todos As Label";
mostCurrent._txt_todos = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 68;BA.debugLine="Private txt_tot_estacionados As Label";
mostCurrent._txt_tot_estacionados = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 69;BA.debugLine="Private txt_tot_retirados As Label";
mostCurrent._txt_tot_retirados = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 70;BA.debugLine="Private bot_atras As Button";
mostCurrent._bot_atras = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 71;BA.debugLine="Private bot_error_ing As Button";
mostCurrent._bot_error_ing = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 72;BA.debugLine="Private msg_error As Label";
mostCurrent._msg_error = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 73;BA.debugLine="Private panel_error_ingreso As Panel";
mostCurrent._panel_error_ingreso = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 74;BA.debugLine="Private bot_invitado As Button";
mostCurrent._bot_invitado = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 75;BA.debugLine="Private bot_exit As Button";
mostCurrent._bot_exit = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 76;BA.debugLine="End Sub";
return "";
}
public static String  _listar_click() throws Exception{
int _ingreso = 0;
 //BA.debugLineNum = 161;BA.debugLine="Sub listar_Click";
 //BA.debugLineNum = 162;BA.debugLine="tot_estacionados = 0";
_tot_estacionados = (int) (0);
 //BA.debugLineNum = 163;BA.debugLine="tot_retirados = 0";
_tot_retirados = (int) (0);
 //BA.debugLineNum = 164;BA.debugLine="Private ingreso As Int = 0";
_ingreso = (int) (0);
 //BA.debugLineNum = 165;BA.debugLine="lista_ins.SingleLineLayout.Label.TextColor = Colo";
mostCurrent._lista_ins.getSingleLineLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 166;BA.debugLine="lista_ins.TwoLinesLayout.Label.TextColor = Colors";
mostCurrent._lista_ins.getTwoLinesLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 167;BA.debugLine="lista_retirados.SingleLineLayout.Label.TextColor";
mostCurrent._lista_retirados.getSingleLineLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 168;BA.debugLine="lista_retirados.TwoLinesLayout.Label.TextColor =";
mostCurrent._lista_retirados.getTwoLinesLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 169;BA.debugLine="lista_todos.SingleLineLayout.Label.TextColor = Co";
mostCurrent._lista_todos.getSingleLineLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 170;BA.debugLine="lista_todos.TwoLinesLayout.Label.TextColor = Colo";
mostCurrent._lista_todos.getTwoLinesLayout().Label.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 171;BA.debugLine="DateTime.DateFormat = \"dd-MM-yyyy\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("dd-MM-yyyy");
 //BA.debugLineNum = 172;BA.debugLine="fecha = DateTime.Date(DateTime.Now)";
mostCurrent._fecha = anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow());
 //BA.debugLineNum = 173;BA.debugLine="Mi_Cursor=Clientes.ExecQuery(\"SELECT * FROM autos";
mostCurrent._mi_cursor.setObject((android.database.Cursor)(_clientes.ExecQuery("SELECT * FROM autos1")));
 //BA.debugLineNum = 174;BA.debugLine="If Mi_Cursor.RowCount>0 Then";
if (mostCurrent._mi_cursor.getRowCount()>0) { 
 //BA.debugLineNum = 176;BA.debugLine="lista_ins.Clear";
mostCurrent._lista_ins.Clear();
 //BA.debugLineNum = 177;BA.debugLine="lista_retirados.Clear";
mostCurrent._lista_retirados.Clear();
 //BA.debugLineNum = 178;BA.debugLine="lista_todos.Clear";
mostCurrent._lista_todos.Clear();
 //BA.debugLineNum = 179;BA.debugLine="For i=0 To Mi_Cursor.RowCount-1";
{
final int step17 = 1;
final int limit17 = (int) (mostCurrent._mi_cursor.getRowCount()-1);
_i = (int) (0) ;
for (;(step17 > 0 && _i <= limit17) || (step17 < 0 && _i >= limit17) ;_i = ((int)(0 + _i + step17))  ) {
 //BA.debugLineNum = 180;BA.debugLine="Mi_Cursor.Position=i";
mostCurrent._mi_cursor.setPosition(_i);
 //BA.debugLineNum = 181;BA.debugLine="If Mi_Cursor.GetString(\"fecha\") = fecha Then";
if ((mostCurrent._mi_cursor.GetString("fecha")).equals(mostCurrent._fecha)) { 
 //BA.debugLineNum = 182;BA.debugLine="If Mi_Cursor.GetString(\"hout\") = \"-\" Then";
if ((mostCurrent._mi_cursor.GetString("hout")).equals("-")) { 
 //BA.debugLineNum = 183;BA.debugLine="tot_estacionados = tot_estacionados + 1";
_tot_estacionados = (int) (_tot_estacionados+1);
 //BA.debugLineNum = 184;BA.debugLine="lista_ins.AddTwoLines(\"Patente: \"&Mi_Cursor.G";
mostCurrent._lista_ins.AddTwoLines(BA.ObjectToCharSequence("Patente: "+mostCurrent._mi_cursor.GetString("patente")),BA.ObjectToCharSequence(" Hora ingreso: "+mostCurrent._mi_cursor.GetString("hin")));
 //BA.debugLineNum = 185;BA.debugLine="lista_todos.AddTwoLines(\"Patente: \"&Mi_Cursor";
mostCurrent._lista_todos.AddTwoLines(BA.ObjectToCharSequence("Patente: "+mostCurrent._mi_cursor.GetString("patente")),BA.ObjectToCharSequence(" Hora ingreso: "+mostCurrent._mi_cursor.GetString("hin")));
 }else {
 //BA.debugLineNum = 187;BA.debugLine="tot_retirados = tot_retirados + 1";
_tot_retirados = (int) (_tot_retirados+1);
 //BA.debugLineNum = 188;BA.debugLine="ingreso = ingreso + Mi_Cursor.GetString(\"mont";
_ingreso = (int) (_ingreso+(double)(Double.parseDouble(mostCurrent._mi_cursor.GetString("monto"))));
 //BA.debugLineNum = 189;BA.debugLine="lista_retirados.AddTwoLines(\"Patente: \"&Mi_Cu";
mostCurrent._lista_retirados.AddTwoLines(BA.ObjectToCharSequence("Patente: "+mostCurrent._mi_cursor.GetString("patente")+"          Monto: $"+mostCurrent._mi_cursor.GetString("monto")),BA.ObjectToCharSequence(" Hora ingreso: "+mostCurrent._mi_cursor.GetString("hin")+"            Hora salida: "+mostCurrent._mi_cursor.GetString("hout")));
 //BA.debugLineNum = 190;BA.debugLine="lista_todos.AddTwoLines(\"Patente: \"&Mi_Cursor";
mostCurrent._lista_todos.AddTwoLines(BA.ObjectToCharSequence("Patente: "+mostCurrent._mi_cursor.GetString("patente")),BA.ObjectToCharSequence(" Hora ingreso: "+mostCurrent._mi_cursor.GetString("hin")+"            Hora salida: "+mostCurrent._mi_cursor.GetString("hout")));
 };
 };
 }
};
 };
 //BA.debugLineNum = 197;BA.debugLine="txt_tot_estacionados.Text = \"CANTIDAD: \" & tot_es";
mostCurrent._txt_tot_estacionados.setText(BA.ObjectToCharSequence("CANTIDAD: "+BA.NumberToString(_tot_estacionados)));
 //BA.debugLineNum = 198;BA.debugLine="txt_tot_retirados.Text = \"CANTIDAD: \" & tot_retir";
mostCurrent._txt_tot_retirados.setText(BA.ObjectToCharSequence("CANTIDAD: "+BA.NumberToString(_tot_retirados)+"               INGRESO TOTAL: $"+BA.NumberToString(_ingreso)));
 //BA.debugLineNum = 199;BA.debugLine="txt_todos.Text = \"CANTIDAD: \" & (tot_estacionados";
mostCurrent._txt_todos.setText(BA.ObjectToCharSequence("CANTIDAD: "+BA.NumberToString((_tot_estacionados+_tot_retirados))));
 //BA.debugLineNum = 200;BA.debugLine="TabHost1.CurrentTab = 3";
mostCurrent._tabhost1.setCurrentTab((int) (3));
 //BA.debugLineNum = 201;BA.debugLine="tab_vistas.CurrentTab = 0";
mostCurrent._tab_vistas.setCurrentTab((int) (0));
 //BA.debugLineNum = 202;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim Clientes As SQL";
_clientes = new anywheresoftware.b4a.sql.SQL();
 //BA.debugLineNum = 20;BA.debugLine="End Sub";
return "";
}
public static String  _update_values(int _idx) throws Exception{
 //BA.debugLineNum = 308;BA.debugLine="Sub update_values(idx As Int)";
 //BA.debugLineNum = 309;BA.debugLine="DateTime.DateFormat = \"HH:mm\"";
anywheresoftware.b4a.keywords.Common.DateTime.setDateFormat("HH:mm");
 //BA.debugLineNum = 310;BA.debugLine="hout = DateTime.Date(DateTime.Now)";
mostCurrent._hout = anywheresoftware.b4a.keywords.Common.DateTime.Date(anywheresoftware.b4a.keywords.Common.DateTime.getNow());
 //BA.debugLineNum = 311;BA.debugLine="Mi_Cursor.Position=idx-1";
mostCurrent._mi_cursor.setPosition((int) (_idx-1));
 //BA.debugLineNum = 312;BA.debugLine="hin = Mi_Cursor.GetString(\"hin\")";
mostCurrent._hin = mostCurrent._mi_cursor.GetString("hin");
 //BA.debugLineNum = 313;BA.debugLine="Clientes.BeginTransaction";
_clientes.BeginTransaction();
 //BA.debugLineNum = 314;BA.debugLine="Private monto As Int = calc_monto(calc_tiempo(hin";
_monto = _calc_monto(_calc_tiempo(mostCurrent._hin,mostCurrent._hout));
 //BA.debugLineNum = 315;BA.debugLine="Try";
try { //BA.debugLineNum = 316;BA.debugLine="Clientes.ExecNonQuery2(\"UPDATE autos1 SET monto";
_clientes.ExecNonQuery2("UPDATE autos1 SET monto = ?, hout = ? WHERE patente = '"+mostCurrent._patente+"'",anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{BA.NumberToString(_monto),mostCurrent._hout}));
 //BA.debugLineNum = 317;BA.debugLine="Clientes.TransactionSuccessful";
_clientes.TransactionSuccessful();
 //BA.debugLineNum = 318;BA.debugLine="Log(\"updated\")";
anywheresoftware.b4a.keywords.Common.Log("updated");
 } 
       catch (Exception e12) {
			processBA.setLastException(e12); //BA.debugLineNum = 320;BA.debugLine="Log(\"catch: \" & LastException.Message)";
anywheresoftware.b4a.keywords.Common.Log("catch: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 };
 //BA.debugLineNum = 322;BA.debugLine="Clientes.EndTransaction";
_clientes.EndTransaction();
 //BA.debugLineNum = 323;BA.debugLine="End Sub";
return "";
}
}
