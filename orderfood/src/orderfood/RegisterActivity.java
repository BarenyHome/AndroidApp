package orderfood;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegisterActivity extends AppCompatActivity {
	public EditText phoneNumber;
    private EditText vCode;
    public String phone_number;
    private Button getcode;
    private Button register_done;
    private String code_number;
    public EditText register_password;
    public String registerPsw;
    EventHandler eventHandler;
    private int time=60;
    private boolean flag=true;
    private Button registerBack;
    String username;
    EditText inputusername;
    Button checkname;
    TextView chechstatus;

    private static final String db_url = "jdbc:mysql://cdb-lp37p5m4.bj.tencentcdb.com:10014/orderfood";
    private static final String user = "root";
    private static final String password = "gbn980413";
    private class Send2 extends AsyncTask<String ,String , String>
    {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                boolean check=false;
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection(db_url, user, password);
                String needcheckname=inputusername.getText().toString();
                String query0 = "select userId from client where userId='"+needcheckname+"'";
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = ((Statement) stmt1).executeQuery(query0);
                while (rs1.next())
                {
                    check=true;
                }
                if(check==true)
                {
                    chechstatus.setText("Exist");
                }
                else
                {
                    chechstatus.setText("Not Exist");
                }
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        phoneNumber = (EditText) findViewById(R.id.etPhoneNo);
        register_password = (EditText)findViewById(R.id.et_registerPsw);

        inputusername=(EditText)findViewById(R.id.etInputName);

        registerBack = (Button) findViewById(R.id.register_back);
        registerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(registerActivity.this,loginActivity.class);
                startActivity(intent);
            }
        });
        chechstatus=(TextView)findViewById(R.id.textViewCheckName);
        checkname=(Button)findViewById(R.id.buttonCheckName);
        checkname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Send2 send22=new Send2();
                send22.execute();
            }
        });


        getId();

        EventHandler handler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;

                if (result == SMSSDK.RESULT_COMPLETE) {

                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(registerActivity.this, "Success", Toast.LENGTH_SHORT).show();
                               // Intent intent = new Intent(registerActivity.this, personalInfo.class);
                                //startActivity(intent);
                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(registerActivity.this, "Done", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {


                    }
                } else {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    try {
                        JSONObject obj = new JSONObject(throwable.getMessage());
                        final String des = obj.optString("detail");
                        if (!TextUtils.isEmpty(des)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(registerActivity.this, "WrongInfo", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        SMSSDK.registerEventHandler(handler);

        register_done=(Button)findViewById(R.id.btnRegisterDone);
        register_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chechstatus.getText().toString().equals("Not Exist"))
                {
                    code_number =  vCode.getText().toString();
                    phone_number = phoneNumber.getText().toString();
                    SMSSDK.submitVerificationCode("86", phone_number,code_number);
                    Send objSend =  new Send();
                    objSend.execute("");
                    Intent intent=new Intent(registerActivity.this,RegisterSuccess.class);
                    startActivity(intent);
                }
            }
        });

    }

    public void getId()
    {
        phoneNumber = (EditText) findViewById(R.id.etPhoneNo);
        vCode = (EditText) findViewById(R.id.etTextMsg);
        getcode = (Button)findViewById(R.id.btnVerification);
        register_done = (Button)findViewById((R.id.btnRegister));
    }


    private class Send extends AsyncTask<String ,String , String>
    {
        String msg = " ";
        @Override
        protected  void onPreExecute(){

        }
        @Override
        protected String doInBackground(String... strings){
            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection(db_url,user,password);
                if(conn ==null){
                    msg = ("Connection goes wrong");
                }
                else{
                    registerPsw = register_password.getText().toString();
                    phone_number = phoneNumber.getText().toString().trim();
                    username=inputusername.getText().toString();
                    Client c = new Client(username,phone_number,registerPsw);
                    Statement stmt = conn.createStatement();
                    String query = "insert into client(phoneNumber,password,userId) values (?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1,c.getPhoneNumber());
                    ps.setString(2,c.getPassword());
                    ps.setString(3,username);

                    ps.executeUpdate();
                    ps.close();
                    msg = "inserting successfully";
                }
             conn.close();
            }catch(Exception e){
                msg = "connection goes wrong";
                e.printStackTrace();
            }
            return msg;
        }
        @Override
        protected void onPostExecute(String msg){

        }
    }

    public void onClick1(View v)
    {
        switch (v.getId())
        {
            case R.id.btnVerification:
                if(judPhone())
                {
                    SMSSDK.getVerificationCode("86",phone_number);
                    vCode.requestFocus();
                }
                break;

            default:
                break;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }


    private boolean judPhone() {
        if (TextUtils.isEmpty(phoneNumber.getText().toString().trim())) {
            Toast.makeText(registerActivity.this, "please enter your phone number", Toast.LENGTH_LONG).show();
            phoneNumber.requestFocus();
            return false;
        } else if (phoneNumber.getText().toString().trim().length() != 11) {
            Toast.makeText(registerActivity.this, "your phone number digits is wrong", Toast.LENGTH_LONG).show();
            phoneNumber.requestFocus();
            return false;
        } else {
            phone_number = phoneNumber.getText().toString().trim();
            String num = "[1][358]\\d{9}";
            if (phone_number.matches(num))
                return true;
            else {
                Toast.makeText(registerActivity.this, "please enter the correct the phone number", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }
}
