package org.foxteam.noisyfox.noexclamation.adapter;

import android.os.Bundle;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public interface ITaskExecutor {
    void runTask(String msg, TaskRunnable task, AfterTaskRunnable after);

    interface AfterTaskRunnable {
        void run(Bundle result);
    }

    interface TaskRunnable {
        Bundle run();
    }
}
