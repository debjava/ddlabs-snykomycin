package com.ddlab.rnd.action;//package com.ddlab.rnd.action;

import com.ddlab.rnd.ui.util.CommonUIUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class OnStartActivity implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        CommonUIUtil.notifyInfo(project, "Please check and fix Snyk vulnerabilities ...");
        return CompletableFuture.completedFuture(null);
    }
}
