package org.counter.utils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stat util
 * 
 * @author tim
 * 
 */
public class TimeStatUtil {
	public static final int maxElapseTimeCount = 100000;

	public static void addElapseTimeStat(List<Integer> elapseTime, int newTime) {
		if (elapseTime.size() >= maxElapseTimeCount) {
			elapseTime.clear();
		}

		elapseTime.add(newTime);
	}

	public static String getElapseTimeStat(List<Integer> elapseTime) {
		try {
			int n = elapseTime.size();
			if (n == 0)
				return "";
			long totalTime = 0;
			int t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0, t7 = 0, t8 = 0, t9 = 0, t10 = 0;
			for (int j = 0; j < n; j++) {
				int t = elapseTime.get(j);
				totalTime += t;
				if (t == 0)
					t1++;
				else if (t < 3)
					t2++;
				else if (t < 5)
					t3++;
				else if (t < 10)
					t4++;
				else if (t < 50)
					t5++;
				else if (t < 100)
					t6++;
				else if (t < 500)
					t7++;
				else if (t < 1000)
					t8++;
				else if (t < 3000)
					t9++;
				else
					t10++;
			}
			double v1 = t1 * 1000l / n / 10.0;
			double v2 = t2 * 1000l / n / 10.0;
			double v3 = t3 * 1000l / n / 10.0;
			double v4 = t4 * 1000l / n / 10.0;
			double v5 = t5 * 1000l / n / 10.0;
			double v6 = t6 * 1000l / n / 10.0;
			double v7 = t7 * 1000l / n / 10.0;
			double v8 = t8 * 1000l / n / 10.0;
			double v9 = t9 * 1000l / n / 10.0;
			double v10 = t10 * 1000l / n / 10.0;

			int j = t1;
			double u1 = j * 1000l / n / 10.0;
			j += t2;
			double u2 = j * 1000l / n / 10.0;
			j += t3;
			double u3 = j * 1000l / n / 10.0;
			j += t4;
			double u4 = j * 1000l / n / 10.0;
			j += t5;
			double u5 = j * 1000l / n / 10.0;
			j += t6;
			double u6 = j * 1000l / n / 10.0;
			j += t7;
			double u7 = j * 1000l / n / 10.0;
			j += t8;
			double u8 = j * 1000l / n / 10.0;
			j += t9;
			double u9 = j * 1000l / n / 10.0;
			j += t10;
			double u10 = j * 1000l / n / 10.0;

			StringBuilder sb = new StringBuilder();
			sb.append("<    1ms ").append(u1).append("% ").append(v1).append("% (").append(t1).append(")\n");
			if (u1 < 99.99)
				sb.append("<    3ms ").append(u2).append("% ").append(v2).append("% (").append(t2).append(")\n");
			if (u2 < 99.99)
				sb.append("<    5ms ").append(u3).append("% ").append(v3).append("% (").append(t3).append(")\n");
			if (u3 < 99.99)
				sb.append("<   10ms ").append(u4).append("% ").append(v4).append("% (").append(t4).append(")\n");
			if (u4 < 99.99)
				sb.append("<   50ms ").append(u5).append("% ").append(v5).append("% (").append(t5).append(")\n");
			if (u5 < 99.99)
				sb.append("<  100ms ").append(u6).append("% ").append(v6).append("% (").append(t6).append(")\n");
			if (u6 < 99.99)
				sb.append("<  500ms ").append(u7).append("% ").append(v7).append("% (").append(t7).append(")\n");
			if (u7 < 99.99)
				sb.append("< 1000ms ").append(u8).append("% ").append(v8).append("% (").append(t8).append(")\n");
			if (u8 < 99.99)
				sb.append("< 3000ms ").append(u9).append("% ").append(v9).append("% (").append(t9).append(")\n");
			if (t10 > 0)
				sb.append("> 3000ms ").append(u10).append("% ").append(v10).append("% (").append(t10).append(")\n");

			sb.append("avg ").append(totalTime / n).append("\n");

			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getElapseTimeStat error");
			return "";
		}
	}

	static DecimalFormat secFormat = new DecimalFormat("###,###,###,###");

	public static void printStat(AtomicLong count, AtomicLong errorCount, List<Integer> elapseTime) {
		long time1 = System.currentTimeMillis();
		long lastCount = 0;
		long lastTime = 0;
		long max = 0;
		int loopCount = 0;
		while (true) {
			long time2 = System.currentTimeMillis();
			if (time2 == 0)
				break;
			if (time2 == time1)
				continue;
			long cnt = count.get();
			long errCount = errorCount.get();
			StringBuilder sb = new StringBuilder();

			long uptime = (time2 - time1) / 1000;
			sb.append("\n");
			sb.append("OK /ERR: ").append(cnt).append("/").append(errCount);

			sb.append(" (Uptime: ").append(secFormat.format(uptime)).append(" in second, ")
					.append(secFormat.format(uptime / 60)).append(" in minute)\n");

			long avg = cnt * 1000l / (time2 - time1);
			long cur = (cnt - lastCount) * 1000l / (time2 - lastTime);
			if (cur > max)
				max = cur;

			sb.append("AVG/CUR/MAX RPS: ");
			sb.append(avg);
			sb.append("/");
			sb.append(cur);
			sb.append("/");
			sb.append(max).append("\n");

			lastTime = time2;
			lastCount = cnt;

			if (elapseTime != null && loopCount++ % 5 == 0) {
				sb.append(TimeStatUtil.getElapseTimeStat(elapseTime));
				sb.append("\n");
			}

			System.out.println(sb.toString());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

	}
}
