package com.abkv.choseone.data;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.abkv.choseone.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class GoogleDriveHandler
{
    public static final String NAME_FOLDER = "com.abkv.choseone";

    public static volatile GoogleDriveHandler mInstance = null;

    // The google drive API client.
    private DriveResourceClient mClient = null;

    public static GoogleDriveHandler getInstance(Context context, Intent intent)
    {
        if (null == mInstance)
        {
            synchronized (GoogleDriveHandler.class)
            {
                if (null == mInstance)
                {
                    mInstance = new GoogleDriveHandler(context, intent);
                }
            }
        }

        return mInstance;
    }

    private GoogleDriveHandler(final Context context, Intent intent)
    {
        Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(intent);

        if (getAccountTask.isSuccessful())
        {
            // Build a drive resource client.
            mClient = Drive.getDriveResourceClient(context, getAccountTask.getResult());
            Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE)).build();
            // [START query_files]
            Task<MetadataBuffer> queryTask = mClient.query(query);
            // [END query_files]

            queryTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>()
            {
                @Override
                public void onSuccess(MetadataBuffer metadata)
                {
                    Logger.e(this, "Folder count: ", metadata.getCount());

                    boolean isFolderExist = false;

                    for (int i = 0; i < metadata.getCount(); i++)
                    {
                        Metadata data = metadata.get(0);
                        Logger.i(this, "Count: ", i, " ", data.getTitle());

                        if (context.getPackageName().equalsIgnoreCase(data.getTitle()))
                        {
                            Logger.i(this, "Find target folder. ", data.getCreatedDate());
                            Logger.i(this, data.getAlternateLink());
                            isFolderExist = true;
                        }
                    }

                    if (!isFolderExist)
                    {
                        createAppFolder();
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Logger.i(this);
                    e.printStackTrace();
                }
            });
        }
    }

    private void createAppFolder()
    {
        Logger.i(this);

        Task<DriveFolder> getRoot = mClient.getRootFolder().continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>()
        {
            @Override
            public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) throws Exception
            {
                return mClient.createFolder(task.getResult(), new MetadataChangeSet.Builder().setTitle(NAME_FOLDER).setMimeType(DriveFolder.MIME_TYPE).setStarred(true).build());
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
                Logger.e(this, "Failure.");
            }
        });
    }
}
