package com.ddlab.rnd.ui.util;

import com.ddlab.rnd.common.util.Constants;
import com.ddlab.rnd.setting.SynkoMycinSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUIUtil {

    public static void showAppSuccessfulMessage(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showInfoMessage(message, Constants.PROD_TITLE));
    }

    public static void showAppErrorMessage(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showErrorDialog(message, Constants.PROD_TITLE));
    }

    public static void notifyInfo(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Snykomycin Notification Group")
                .createNotification("Snykmycin", message, NotificationType.INFORMATION)
                .notify(project);
    }

    public static void showErrorNotifiation(String msg) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Snykomycin Notification Group")
                .createNotification(msg, NotificationType.ERROR)
                .notify(projects[0]);
    }

    public static void showWarningNotifiation(Project project, String msg) {
        Notification notification =
                new Notification("Snykomycin Notification Group", Constants.PROD_TITLE, msg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }

    public static void showWarningNotifiation(String msg) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            Notification notification =
                    new Notification("Snykomycin Notification Group", Constants.PROD_TITLE, msg, NotificationType.INFORMATION);
            Notifications.Bus.notify(notification, project);
        }
    }

    public static void validateAiInputsFromSetting() {
        SynkoMycinSettings setting = SynkoMycinSettings.getInstance();
        String clientId = setting.getClientIdStr();
        String clientSecret = setting.getClientSecretStr();
        String oAuthTokenUrl = setting.getOauthEndPointUri();
        String aiApiEndPointUri = setting.getLlmApiEndPointUri();
        String snykApiEndPointUri = setting.getSnykUriTxt();
        String snykToken = setting.getSnykTokenTxt();

        if (clientId == null || clientId.isEmpty()) {
            Messages.showErrorDialog(Constants.CLIENT_ID_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.CLIENT_ID_ERR_MSG);
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            Messages.showErrorDialog(Constants.CLIENT_SECRET_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.CLIENT_SECRET_ERR_MSG);
        }
        if (oAuthTokenUrl == null || oAuthTokenUrl.isEmpty()) {
            Messages.showErrorDialog(Constants.OAUTH_END_PT_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.OAUTH_END_PT_ERR_MSG);
        }
        if (aiApiEndPointUri == null || aiApiEndPointUri.isEmpty()) {
            Messages.showErrorDialog(Constants.LLM_API_END_PT_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.LLM_API_END_PT_ERR_MSG);
        }

        if (snykApiEndPointUri == null || snykApiEndPointUri.isEmpty()) {
            Messages.showErrorDialog(Constants.SNYK_API_END_PT_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.SNYK_API_END_PT_ERR_MSG);
        }

        if (snykToken == null || snykToken.isEmpty()) {
            Messages.showErrorDialog(Constants.SNYK_TKN_ERR_MSG, Constants.PROD_TITLE);
            throw new IllegalArgumentException(Constants.SNYK_TKN_ERR_MSG);
        }

    }

    public static void createBackFile(Project project, String buildFileName) {
        createBackupAndCopy(project, buildFileName);

        // Refresh the project
        String basePath = project.getBasePath();
        if (basePath != null) {
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(basePath);
            if (baseDir != null) {
                baseDir.refresh(true, true); // recursive refresh
            }
        }

    }

    private static void createBackupAndCopy(Project project, String buildFileName) {
        String formattedDate = new SimpleDateFormat(Constants.DATE_FMT).format(new Date());
        String projectBasePath = project.getBasePath();
        File backupDir = new File(projectBasePath + File.separator + Constants.BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        Path srcBasePath = Paths.get(projectBasePath, buildFileName);
        Path backDestnFilePath = Paths.get(backupDir.getAbsolutePath(), buildFileName + "." + formattedDate);

        try {
//            Files.write(backFilePath, buildFileText.getBytes());
            Files.copy(srcBasePath, backDestnFilePath);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
