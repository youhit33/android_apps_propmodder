package com.n00bware.propmodder;
import com.n00bware.propmodder.R;

import java.io.*;
import java.lang.*;
import java.text.*;
import android.*;
import android.app.*;
import android.app.Dialog;
import android.content.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import android.util.Log;

public class helper {
    public static String TAG = "PropModder";

    public static boolean runRootCommand(String command) {
        Log.i(TAG, "runRootCommand started");
        Process process = null;
        DataOutputStream os = null;
        try {
            Log.i(TAG, "attempt to get root");
	    process = Runtime.getRuntime().exec("su");
	    os = new DataOutputStream(process.getOutputStream());
	    os.writeBytes(command+"\n");
	    os.writeBytes("exit\n");
	    os.flush();
	    process.waitFor();
	} catch (Exception e) {
	    Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
	return false;
	}
        finally {
            try {
	        if (os != null) os.close();
	            process.destroy();
	        } catch (Exception e) {}
	    }
        return true;
    }

    public static boolean RemountRW(){
        Log.i(TAG, "Mount /system as READ/WRITE: RemountRW");
        return helper.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system");
    }

    public static boolean RemountROnly(){
        Log.i(TAG, "Mount /system as READ ONLY: RemountROnly");
        return helper.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
    }

    public static void SetProp(String CHOKE_PROP, String CHOKE_VALUE){
        Log.i(TAG, "start SetProp method with args: " + CHOKE_PROP + " & " + CHOKE_VALUE);
        try {
            helper.RemountRW();
            BufferedReader in = new BufferedReader(new FileReader("/system/build.prop"));
            PrintWriter out = new PrintWriter(new File("/tmp/build.prop"));

            String line;
            String params[];

            while ((line = in.readLine()) != null) {
                Log.i(TAG, "Reading in while statement" + line);
                params = line.split("="); // some devices have values in ' = ' format vs '='
                if (params[0].equalsIgnoreCase("CHOKE_PROP ") ||
                    params[0].equalsIgnoreCase("CHOKE_PROP")) {
                    out.println(CHOKE_PROP + "=" + CHOKE_VALUE);
                    Log.e(TAG, "***_CHANGED_*** println @ " + line);
                } else {
                    out.println(line);
                    Log.e(TAG, "unchanged: " + line);
                    }
                }

                in.close();
                out.flush();
                out.close();

                // open su shell and write commands to the OutStream for execution
                Log.i(TAG, "***START HACKING***");
                Process p = Runtime.getRuntime().exec("su");
                PrintWriter pw = new PrintWriter(p.getOutputStream());
                pw.println("busybox mount -o remount,rw /system");
                pw.println("cp /tmp/build.prop /system/build.prop");
                pw.println("exit");
                pw.close();

        }catch(Exception e) { e.printStackTrace();
           Log.e(TAG, "***DEBUG***: " + e);}
    }

    public static void ClearChokes(String CHOKE_PROP, String CHOKE_VALUE){
        Log.i(TAG, "SENDING values of CHOKE_PROP and CHOKE_VALUES to null " + CHOKE_PROP + " " + CHOKE_VALUE);
        CHOKE_PROP = " ";
        CHOKE_VALUE = " ";
        Log.i(TAG, "values of CHOKE_PROP and CHOKE_VALUES should be whitespace: " + "{" + CHOKE_PROP + "} {" + CHOKE_VALUE + "}");
    }
}
