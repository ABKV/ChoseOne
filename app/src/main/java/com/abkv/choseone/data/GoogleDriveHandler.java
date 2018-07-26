package com.abkv.choseone.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.abkv.choseone.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.atomic.AtomicBoolean;

public class GoogleDriveHandler
{
    // The google drive API client.
    private DriveResourceClient mClient = null;

    private boolean mIsDataExist = false;

    public GoogleDriveHandler(DriveResourceClient driveClient)
    {
        mClient = driveClient;
        checkDataExist();
    }

    public GoogleDriveHandler(final Context context, Intent intent)
    {
        Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(intent);

        if (getAccountTask.isSuccessful())
        {
            // Build a drive resource client.
            final DriveResourceClient resourceClient = Drive.getDriveResourceClient(context, getAccountTask.getResult());
            Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE)).build();
            // [START query_files]
            Task<MetadataBuffer> queryTask = resourceClient.query(query);
            // [END query_files]

            Tasks.whenAll(queryTask).continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>()
            {
                @Override
                public Task<DriveFolder> then(@NonNull Task<MetadataBuffer> task) throws Exception
                {
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<TContinuationResult>()
            {
                @Override
                public void onSuccess(TContinuationResult tContinuationResult)
                {

                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {

                }
            });

            Task<DriveFolder> getRoot = resourceClient.getRootFolder().continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>()
            {
                @Override
                public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) throws Exception
                {
                    return resourceClient.createFolder(task.getResult(), new MetadataChangeSet.Builder().setTitle(context.getPackageName()).setMimeType(DriveFolder.MIME_TYPE).setStarred(true).build());
                }
            }).addOnSuccessListener(new OnSuccessListener<DriveFolder>()
            {
                @Override
                public void onSuccess(DriveFolder driveFolder)
                {
                    Logger.i(this, driveFolder.getDriveId().encodeToString());
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    private void checkDataExist()
    {
        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE)).build();
        // [START query_files]
        Task<MetadataBuffer> queryTask = mClient.query(query);
        // [END query_files]
        // [START query_results]
        queryTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>()
        {
            @Override
            public void onSuccess(MetadataBuffer metadata)
            {
                for (int i=0; i<metadata.getCount(); i++)
                {
                    Logger.i(this, metadata.get(i).getTitle());
                    if (mClient.getApplicationContext().getPackageName().equals(metadata.get(i).getTitle()))
                    {
                        mIsDataExist = true;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
                Logger.w(this, "Query failed.");
            }
        });
        // [END query_results]
    }

    public boolean isDataExist()
    {
        return mIsDataExist;
    }
}
