package com.dianping.cat.report.page.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.service.ReportConstants;

public class TransactionMergeManager {

	public TransactionReport mergerAll(TransactionReport report, String ipAddress, String allName) {
		TransactionReport temp = mergerAllIp(report, ipAddress);

		return mergerAllName(temp, allName);
	}

	public TransactionReport mergerAllIp(TransactionReport report, String ipAddress) {
		if (ReportConstants.ALL.equalsIgnoreCase(ipAddress)) {
			MergeAllMachine all = new MergeAllMachine();
			
			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	private TransactionReport mergerAllName(TransactionReport report, String allName) {
		if (ReportConstants.ALL.equalsIgnoreCase(allName)) {
			MergeAllName all = new MergeAllName();
			
			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

}
