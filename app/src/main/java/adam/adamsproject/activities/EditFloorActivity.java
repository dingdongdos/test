package adam.adamsproject.activities;

import adam.adamsproject.R;
import adam.adamsproject.fragments.DeleteFloorplanDialog;
import adam.adamsproject.fragments.NewFloorDialog;
import adam.adamsproject.fragments.NewMachineDialog;
import adam.adamsproject.model.Floor;
import adam.adamsproject.model.Machine;
import adam.adamsproject.util.FloorFileManager;
import adam.adamsproject.views.FloorPlanView;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

public class EditFloorActivity extends AppCompatActivity implements NewMachineDialog.NewMachineListener, DeleteFloorplanDialog.DeleteFloorplanListener
{
    public static final String TAG = "EditFloorActivity";
    public static final String FLOOR_FILE_SUFFIX = ".floor";
    File mFile;
    Floor mFloor;
    FloorPlanView floorPlanView;
    Button deleteMachine;
    Button copyMachine;
    TextView currentMachineName;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_floor);

        floorPlanView = findViewById(R.id.floorEditView);
        deleteMachine = findViewById(R.id.buttonDeleteMachine);
        copyMachine = findViewById(R.id.buttonCopyMachine);
        currentMachineName=findViewById(R.id.textCurrentMachineName);
        Intent startIntent = getIntent();
        String fileName = startIntent.getStringExtra(LoadFloorActivity.EXTRA_FLOOR_FILE);
        if (fileName == null)
        {
            mFile = null;
            String name = startIntent.getStringExtra(NewFloorDialog.EXTRA_FLOOR_NAME);
            float width = startIntent.getFloatExtra(NewFloorDialog.EXTRA_FLOOR_WIDTH, 0);
            float length = startIntent.getFloatExtra(NewFloorDialog.EXTRA_FLOOR_LENGTH, 0);
            mFloor = new Floor(name, width, length);
        }
        else
        {
            mFile = new File(fileName);
            try
            {
                DataInputStream in = new DataInputStream(new FileInputStream(mFile));
                mFloor = Floor.readFloor(in);
                in.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error loading files to view.");
                e.printStackTrace();
                super.onBackPressed();
                return;
            }
        }
        copyMachine.setEnabled(false);
        deleteMachine.setEnabled(false);
        floorPlanView.initialize(mFloor, new FloorPlanView.OnMachineSelectChangeListener()
        {
            @Override
            public void onMachineSelected(boolean selected)
            {
                copyMachine.setEnabled(selected);
                deleteMachine.setEnabled(selected);
                currentMachineName.setText(String.format("Current Machine: %s", floorPlanView.getMachineName()));
            }
        });
        floorPlanView.updateScrollManager();
        setTitle(mFloor.getmName());
    }

    public static File getNewFile(File dir, String name, String suffix)
    {
        File result = new File(dir, name+suffix);
        for (int i = 1; result.exists(); i++)
        {
            result = new File(dir, String.format("%s (%d)%s", name, i, suffix));
        }
        return result;
    }
    public void updateFloorVariables()
    {
        floorPlanView.dropMachine();
    }
    public void save(File file)
    {
        updateFloorVariables();
        Toast saving = Toast.makeText(this, R.string.toast_saving, Toast.LENGTH_LONG);
        saving.show();
        String noteTitle = mFloor.getmName();
        if (file == null)
        {
            file= getNewFile(FloorFileManager.getFloorDirectory(this), noteTitle, FLOOR_FILE_SUFFIX);
        }
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
            mFloor.writeFloor(out);
            out.flush();
            out.close();
            saving.cancel();
            Toast saved = Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT);
            saved.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast failed = Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT);
            failed.show();
        }
    }
    public void onNewMachineClick(View view)
    {
        DialogFragment dialog= new NewMachineDialog();
        dialog.show(getSupportFragmentManager(),"NewMachineDialogFragment");
    }
    @Override
    public void onNewMachineCreated(DialogFragment dialog, String name, float width, float length, String[] tags, boolean organized, boolean cleaned, boolean broken)
    {
        floorPlanView.dropMachine();
        floorPlanView.addMachine(name, tags, width, length, organized, cleaned, broken);
        floorPlanView.postInvalidate();
    }
    @Override
    public void onBackPressed()
    {
        save(mFile);
        super.onBackPressed();
    }
    public void onSaveFloorPlanPressed(View view){
        save(mFile);
        super.onBackPressed();
    }
    public void onDeleteMachineClick(View view){
        floorPlanView.deleteMachine();
    }
    public void onCopyMachineClick(View view){
        floorPlanView.copyMachine();
    }
    public void onDeleteFloorplanClick(View view){
        DialogFragment dialog=new DeleteFloorplanDialog();
        dialog.show(getSupportFragmentManager(),"DeleteFloorplanDialogFragment");
    }

    @Override
    public void onFloorplanDeleted(DialogFragment dialog) {
        if(mFile!=null){
            mFile.delete();
            super.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }
}
