package com.dianping.cat.report.task.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.sql.SqlReportMerger;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.service.ReportConstants;

public class SqlMerger {

	private SqlReport buildDailyReport(List<SqlReport> reports, String reportDomain, boolean allDatabase) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportDomain));
		if (allDatabase) {
			merger.setAllDatabase(true);
		}
		for (SqlReport report : reports) {
			report.accept(merger);
		}
		SqlReport sqlReport = merger.getSqlReport();
		return sqlReport;
	}

	public SqlReport mergeForDaily(String reportDomain, List<SqlReport> reports, Set<String> domains) {
		SqlReport sqlReport = buildDailyReport(reports, reportDomain, false);
		SqlReport sqlReport2 = buildDailyReport(reports, reportDomain, true);

		sqlReport.addDatabase(sqlReport2.findOrCreateDatabase(ReportConstants.ALL));
		sqlReport.getDomainNames().add(ReportConstants.ALL);
		sqlReport.getDomainNames().addAll(domains);

		Date date = sqlReport.getStartTime();
		sqlReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

}
