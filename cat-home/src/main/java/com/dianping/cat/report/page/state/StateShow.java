package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.service.ReportConstants;

public class StateShow extends BaseVisitor {

	private Machine m_total = new Machine();

	private Map<Long, Message> m_messages = new LinkedHashMap<Long, Message>();

	private Map<String, ProcessDomain> m_processDomains = new LinkedHashMap<String, ProcessDomain>();

	private String m_currentIp;

	private String m_ip;

	public StateShow(String ip) {
		m_ip = ip;
	}

	public List<Message> getMessages() {
		List<Message> all = new ArrayList<Message>(m_messages.values());
		List<Message> result = new ArrayList<Message>();
		long current = System.currentTimeMillis();

		for (Message message : all) {
			if (message.getId() < current) {
				result.add(message);
			}
		}
		return result;
	}

	public Map<Long, Message> getMessagesMap() {
		return m_messages;
	}

	public List<ProcessDomain> getProcessDomains() {
		List<ProcessDomain> temp = new ArrayList<ProcessDomain>(m_processDomains.values());
		Collections.sort(temp, new DomainCompartor());
		return temp;
	}
	
	public Map<String,ProcessDomain> getProcessDomainMap() {
		return m_processDomains;
	}

	public Machine getTotal() {
		return m_total;
	}

	public boolean isIp(String ip) {
		boolean result = false;
		// try {
		// if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
		// String s[] = ip.split("\\.");
		//
		// if (Integer.parseInt(s[0]) <= 255) {
		// if (Integer.parseInt(s[1]) <= 255) {
		// if (Integer.parseInt(s[2]) <= 255) {
		// if (Integer.parseInt(s[3]) <= 255) {
		// result = true;
		// }
		// }
		// }
		// }
		// }
		// } catch (Exception e) {
		// //ignore
		// }
		try {
			char first = ip.charAt(0);
			char next = ip.charAt(1);
			if (first >= '0' && first <= '9') {
				if (next >= '0' && next <= '9') {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public int getTotalSize() {
		Set<String> ips = new HashSet<String>();

		for (ProcessDomain domain : m_processDomains.values()) {
			Set<String> temp = domain.getIps();

			for (String str : temp) {
				if (isIp(str)) {
					ips.add(str);
				}
			}
		}
		return ips.size();
	}

	private Machine mergerMachine(Machine total, Machine machine) {
		total.setAvgTps(total.getAvgTps() + machine.getAvgTps());
		total.setTotal(total.getTotal() + machine.getTotal());
		total.setTotalLoss(total.getTotalLoss() + machine.getTotalLoss());
		total.setDump(total.getDump() + machine.getDump());
		total.setDumpLoss(total.getDumpLoss() + machine.getDumpLoss());
		total.setSize(total.getSize() + machine.getSize());
		total.setDelaySum(total.getDelaySum() + machine.getDelaySum());
		total.setDelayCount(total.getDelayCount() + machine.getDelayCount());
		total.setBlockTotal(total.getBlockTotal() + machine.getBlockTotal());
		total.setBlockLoss(total.getBlockLoss() + machine.getBlockLoss());
		total.setBlockTime(total.getBlockTime() + machine.getBlockTime());
		total.setPigeonTimeError(total.getPigeonTimeError() + machine.getPigeonTimeError());
		total.setNetworkTimeError(total.getNetworkTimeError() + machine.getNetworkTimeError());

		if (machine.getMaxTps() > total.getMaxTps()) {
			total.setMaxTps(machine.getMaxTps());
		}

		long count = total.getDelayCount();
		double sum = total.getDelaySum();
		if (count > 0) {
			total.setDelayAvg(sum / count);
		}

		return total;
	}

	private void mergerMessage(Message total, Message message) {
		total.setDelayCount(total.getDelayCount() + message.getDelayCount());
		total.setDelaySum(total.getDelaySum() + message.getDelaySum());
		total.setDump(total.getDump() + message.getDump());
		total.setDumpLoss(total.getDumpLoss() + message.getDumpLoss());
		total.setSize(total.getSize() + message.getSize());
		total.setTotal(total.getTotal() + message.getTotal());
		total.setTotalLoss(total.getTotalLoss() + message.getTotalLoss());
		total.setBlockTotal(total.getBlockTotal() + message.getBlockTotal());
		total.setBlockLoss(total.getBlockLoss() + message.getBlockLoss());
		total.setBlockTime(total.getBlockTime() + message.getBlockTime());
		total.setPigeonTimeError(total.getPigeonTimeError() + message.getPigeonTimeError());
		total.setNetworkTimeError(total.getNetworkTimeError() + message.getNetworkTimeError());
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		m_currentIp = ip;

		if (m_total == null) {
			m_total = new Machine();
			m_total.setIp(ip);
		}
		if (m_ip.equals(ReportConstants.ALL) || m_ip.equalsIgnoreCase(ip)) {
			m_total = mergerMachine(m_total, machine);
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitMessage(Message message) {
		Message temp = m_messages.get(message.getId());
		if (temp == null) {
			m_messages.put(message.getId(), message);
		} else {
			mergerMessage(temp, message);
		}
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		if (m_ip.equals(m_currentIp) || m_ip.equals(ReportConstants.ALL)) {
			ProcessDomain temp = m_processDomains.get(processDomain.getName());

			if (temp == null) {
				m_processDomains.put(processDomain.getName(), processDomain);
			} else {
				temp.getIps().addAll(processDomain.getIps());
			}
		}
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		super.visitStateReport(stateReport);
	}

	public static class DomainCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
