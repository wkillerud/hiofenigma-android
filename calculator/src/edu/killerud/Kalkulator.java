package edu.killerud;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;


/**
 * Created with IntelliJ IDEA.
 * User: William
 * Date: 06.09.12
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class Kalkulator extends Activity implements View.OnClickListener
{

    float currentTotal;

    @Override
    protected void onCreate(Bundle savedInstanceSate)
    {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.main);

        findViewById(R.id.calculate).setOnClickListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.operator);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operators_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if(savedInstanceSate != null)
        {
            currentTotal = savedInstanceSate.getFloat("edu.killerud.Kalkulator.CurrentTotal");
            updateInterfaceWithResult();
        }else{
            currentTotal = 0;
            updateInterfaceWithResult();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putFloat("edu.killerud.Kalkulator.CurrentTotal", currentTotal);
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.calculate:
                performCalculation();
                break;
            default:
                break;
        }
    }

    private void performCalculation()
    {
        try{
            float parsedUserInput = parseUserInput();
            String operationToBePerformed = ((Spinner) findViewById(R.id.operator)).getSelectedItem().toString();
            if(isValidOperation(operationToBePerformed, parsedUserInput))
            {
                executeOperation(operationToBePerformed, parsedUserInput);
                updateInterfaceWithResult();
            }else{
                notifyUserOfFailure();
            }
        }catch (NumberFormatException e)
        {
            notifyUserOfFailure();
            return;
        }
    }

    private void notifyUserOfFailure()
    {
        Toast.makeText(getApplicationContext(),
                R.string.failure, Toast.LENGTH_SHORT).show();
    }

    private void updateInterfaceWithResult()
    {
        ((TextView) findViewById(R.id.result)).setText(""+currentTotal);
    }

    private void executeOperation(String operationToBePerformed, float parsedUserInput)
    {
        if(operationToBePerformed.equals("+"))
        {
            currentTotal = currentTotal + parsedUserInput;
        }else if(operationToBePerformed.equals("-"))
        {
            currentTotal = currentTotal - parsedUserInput;
        }else if(operationToBePerformed.equals("*"))
        {
            currentTotal = currentTotal * parsedUserInput;
        }else if(operationToBePerformed.equals("/"))
        {
            currentTotal = currentTotal / parsedUserInput;
        }
    }

    private boolean isValidOperation(String operationToBePerformed, float parsedUserInput)
    {
        if(operationToBePerformed.equals("/") && parsedUserInput == 0)
        {
            return false;
        }else{
            return true;
        }
    }

    private float parseUserInput()
    {
        return Float.parseFloat(((EditText) findViewById(R.id.input)).getText().toString());
    }


}
