package com.ddlab.rnd.ui.util;

import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.ddlab.rnd.common.util.CallApiType;
import com.ddlab.rnd.common.util.Constants;
import com.ddlab.rnd.snyk.ai.out.model.SnykProjectIssues;
import com.ddlab.rnd.snyk.model.OrgDetails;
import com.intellij.ui.table.JBTable;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class SnykUiUtil {

    public static List<String> getSnykOrgGroupNames(String snykCoreUri, String snykAuthToken) throws RuntimeException {
//        check();
        List<String> orgGroupNames = null;
        try {
            snykCoreUri = snykCoreUri.endsWith("/") ? snykCoreUri + "orgs" : snykCoreUri + "/orgs";
            snykAuthToken = !snykAuthToken.startsWith("token ") ? "token " + snykAuthToken : snykAuthToken;
            String responseBody = CallApiType.GET.perform(snykCoreUri, snykAuthToken);

            ObjectMapper mapper = new ObjectMapper();
            OrgDetails orgDetails = mapper.readValue(responseBody, OrgDetails.class);
            orgGroupNames = orgDetails.getOrgs().stream().map(value -> {
                String orgId = value.getId();
                String orgName = value.getName();
                String groupName = value.getGroup().getName();
                return orgId + "~" + orgName + "~" + groupName;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Unable to fetch Snyk org and group names: ", e);
            throw new RuntimeException("Unable to get response from Snyk, recheck Snyk details and try again");
        }
        return orgGroupNames;
    }

    public static JTable getUpdatedSnykIssueTable(SnykProjectIssues allProjectIssue) {
        JTable table = null;
        AtomicInteger counter = new AtomicInteger(1);
        if (allProjectIssue == null) {
            return null;
        }

        if (allProjectIssue.getIssues() != null || allProjectIssue.getIssues().isEmpty()) {
            List<Object[]> tableData = allProjectIssue.getIssues().stream().map(value -> {
                Integer index = counter.getAndIncrement();
                String artifactName = value.getPkgName();
                String currentVersions = String.join(", ", value.getPkgVersions());
                String severity = value.getSeverity();
                boolean isFixable = value.getFixInfo().getIsFixable();
                String fixedVersions = String.join(", ", value.getFixInfo().getFixedIn());
                return new Object[]{index, artifactName, severity, isFixable, currentVersions, fixedVersions};
            }).collect(Collectors.toList());

            String[] columnHeaders = {Constants.HASH, Constants.ARTIFACT_NAME, Constants.SEVERITY, Constants.FIXABLE, Constants.CURRENT_VERSIONS, Constants.FIXED_VERSIONS};
            Object[][] rows = tableData.toArray(new Object[0][]);

            DefaultTableModel model = new DefaultTableModel(rows, columnHeaders);
            table = new JBTable(model);

            // Enable Sorting also
            table.setAutoCreateRowSorter(true);

            reSizeTable(table);
        }

        return table;
    }

    /**
     * Re size table.
     *
     * @param table the table
     */
    private static void reSizeTable(JTable table) {
        // Make header bold
        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        // Adjust column widths to fit content
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            int width = 50;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                width = Math.max(width, comp.getPreferredSize().width);
            }
            column.setPreferredWidth(width);
        }
    }

}
