package fr.wildcodeschool.wildquizz;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CreateQuizzActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    FirebaseDatabase mDatabase;
    DatabaseReference mQuizzRef;

    private TextView mTvQcmNameValue;
    private EditText mEditQcmNameValue;
    private TextView mTvQuestionValue;
    private EditText mEditQuestionValue;
    private TextView mTvAnswer1Value;
    private EditText mEditAnswer1Value;
    private TextView mTvAnswer2Value;
    private EditText mEditAnswer2Value;
    private TextView mTvAnswer3Value;
    private EditText mEditAnswer3Value;
    private TextView mTvAnswer4Value;
    private EditText mEditAnswer4Value;
    private Button mButtonUpdate;
    private Button mButtonDelete;
    private ListView mListqcmValue;
    String mIdQuizz;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quizz);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        mIdQuizz = intent.getStringExtra("idQuizz");

        if (mIdQuizz == null) {
            //récupérer les données du menu pour la génération de quizz
            String key1 = "123456789";
            String key2 = "abcdefghijklmnopqrstuvwxyz";
            mIdQuizz = String.format("%s%s%s", generateString(3, key1), generateString(2, key2), generateString(3, key1));

            // créer le quizz vide dans Firebase
            QuizzModel quizzModel = new QuizzModel(mIdQuizz, new Date().getTime(), new ArrayList<QcmModel>(), false);
            database.getReference("Quizz").child(mIdQuizz).setValue(quizzModel);
        }

        TextView tvIdQuizz = findViewById(R.id.tv_id_generate);
        tvIdQuizz.setText(mIdQuizz);
        // TODO V2 : vérifier que l'id unique n'existe pas déjà, si c'est le cas, le regénérer

        FloatingActionButton addQcm = findViewById(R.id.floating_add_qcm);
        addQcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToCreateQcm = new Intent(CreateQuizzActivity.this, CreateQcmActivity.class);
                goToCreateQcm.putExtra("idQuizz", mIdQuizz);
                CreateQuizzActivity.this.startActivity(goToCreateQcm);
            }
        });

        // charger la liste des QCM du Quizz à partir de Firebase
        final ArrayList<QcmModel> qcmModels = new ArrayList<>();
        final QcmAdapter adapter = new QcmAdapter(this, 0, qcmModels);
        ListView lvListRoom = findViewById(R.id.list_qcm);
        lvListRoom.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance();
        mQuizzRef = mDatabase.getReference("Quizz").child(mIdQuizz).child("qcmList");
        // Read from the database
        mQuizzRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                qcmModels.clear(); // vide la liste par précaution
                for (DataSnapshot qcmSnapshot : dataSnapshot.getChildren()) {
                    QcmModel qcmModel = qcmSnapshot.getValue(QcmModel.class);
                    qcmModels.add(qcmModel);
                }
                adapter.notifyDataSetChanged(); // met au courant l'adapter que la liste a changé
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        lvListRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                QcmModel qcmModel = qcmModels.get(i);
                showUpdateDialog(qcmModel.getTheme(), qcmModel.getQuestion(), qcmModel.getAnswer1(), qcmModel.getAnswer2(), qcmModel.getAnswer3(), qcmModel.getAnswer4());

            }
        });
        lvListRoom.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                QcmModel qcmModel = qcmModels.get(i);
                showUpdateDialog(qcmModel.getTheme(), qcmModel.getQuestion(), qcmModel.getAnswer1(), qcmModel.getAnswer2(), qcmModel.getAnswer3(), qcmModel.getAnswer4());

                return false;
            }
        });




        //Navigation Drawer :
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_create);
        mToggle = new ActionBarDrawerToggle(CreateQuizzActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation View :
        NavigationView navigationView = findViewById(R.id.nav_view_create);
        navigationView.setNavigationItemSelectedListener(this);
    }



    //String 1 composé de 3 chiffres
    private String generateString(int length, String key) {
        //char[] chars = "abcdefghijklmnopqrstuvwxyz123456789".toCharArray();
        char[] char1 = key.toCharArray();
        StringBuilder stringBuilder1 = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = char1[random.nextInt(char1.length)];
            stringBuilder1.append(c);
        }
        return stringBuilder1.toString();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent goToHome = new Intent(this, MenuActivity.class);
            this.startActivity(goToHome);
        } else if (id == R.id.join) {
            Intent goToJoin = new Intent(this, JoinQuizzActivity.class);
            this.startActivity(goToJoin);
        } else if (id == R.id.create) {
            Intent goToCreate = new Intent(this, CreateQuizzActivity.class);
            this.startActivity(goToCreate);
        } else if (id == R.id.profile) {
            Intent goToProfile = new Intent(this, ProfileActivity.class);
            this.startActivity(goToProfile);
        } else if (id == R.id.displayquizz) {
            Intent goToDisplayQuizz = new Intent(this, DisplayQuizzActivity.class);
            this.startActivity(goToDisplayQuizz);
        } else if (id == R.id.logout) {
            //Déconnexion
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateQcm(String qcm, String ask, String ans1, String ans2, String ans3, String ans4){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Quizz");

        QcmModel qcmModel = new QcmModel();

        //databaseReference.setValue();

        Toast.makeText(this, R.string.updated_qcm, Toast.LENGTH_SHORT).show();

    }


    private void showUpdateDialog(String qcm, String ask, String ans1, String ans2, String ans3, String ans4) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog, null);

        dialogBuilder.setView(dialogView);

        mTvQcmNameValue = dialogView.findViewById(R.id.text_qcm);
        mEditQcmNameValue = dialogView.findViewById(R.id.edit_qcm);
        mTvQuestionValue = dialogView.findViewById(R.id.text_question);
        mEditQuestionValue = dialogView.findViewById(R.id.edit_question);
        mTvAnswer1Value =  dialogView.findViewById(R.id.tv_answer1);
        mEditAnswer1Value =  dialogView.findViewById(R.id.edit_answer1);
        mTvAnswer2Value =  dialogView.findViewById(R.id.tv_answer2);
        mEditAnswer2Value =  dialogView.findViewById(R.id.edit_answer_2);
        mTvAnswer3Value =  dialogView.findViewById(R.id.tv_answer3);
        mEditAnswer3Value = dialogView.findViewById(R.id.edit_answer_3);
        mTvAnswer4Value = dialogView.findViewById(R.id.tv_answer4);
        mEditAnswer4Value = dialogView.findViewById(R.id.edit_answer4);
        mButtonUpdate = dialogView.findViewById(R.id.button_update);
        mListqcmValue =dialogView.findViewById(R.id.list_qcm);
        mButtonDelete = dialogView.findViewById(R.id.button_delete);

        dialogBuilder.setTitle(String.format("%s%s", getString(R.string.update_qcm), qcm));





        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String qcm = mEditQcmNameValue.getText().toString();
                String ask = mEditQuestionValue.getText().toString();
                String ans1 = mEditAnswer1Value.getText().toString();
                String ans2 = mEditAnswer2Value.getText().toString();
                String ans3 = mEditAnswer3Value.getText().toString();
                String ans4 = mEditAnswer4Value.getText().toString();
                int correctAnswer = 1;//TODO récupérer le numéro de la réponse correcte

               final QcmModel qcmModel = new QcmModel(qcm,ask,ans1,ans2,ans3,ans4,correctAnswer);
               addQcmToDB(mEditQcmNameValue.getText().toString());



               Intent goToCreateQuizz = new Intent(CreateQuizzActivity.this, CreateQuizzActivity.class);
               CreateQuizzActivity.this.startActivity(goToCreateQuizz);

            }
        });

        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference drQuizzId = FirebaseDatabase.getInstance().getReference("Quizz").child("quizzid");
                drQuizzId.removeValue();



                String qcm = mEditQcmNameValue.getText().toString().trim();
                String ask = mEditQuestionValue.getText().toString();
                String ans1 = mEditAnswer1Value.getText().toString();
                String ans2 = mEditAnswer2Value.getText().toString();
                String ans3 = mEditAnswer3Value.getText().toString();
                String ans4 = mEditAnswer4Value.getText().toString();


            }
        });



        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();





    }

    private void addQcmToDB(String s) {
    }
    private boolean updateQcm() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Quizz");

        QcmModel qcmModel = new QcmModel();

        databaseReference.setValue(qcmModel);

        Toast.makeText(this, R.string.qcm_update_success, Toast.LENGTH_LONG).show();

        return true;
    }




}

