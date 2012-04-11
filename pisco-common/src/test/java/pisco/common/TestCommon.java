package pisco.common;

import static junit.framework.Assert.*;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;

import java.util.Arrays;

import junit.framework.Assert;

import org.hamcrest.core.IsSame;
import org.junit.Test;

import choco.visu.components.chart.ChocoChartFactory;

public class TestCommon {


	private static PJob[] buildInstance() {
		final int[] p =  {10, 6, 8, 4, 6};
		final int[] rd =  {0, 3, 5 , 7, 8};
		final int[] dd =  {15, 10, 25, 14, 12};
		final int n = p.length;
		PJob[] jobs = new PJob[n];
		for (int i = 0; i < n; i++) {
			jobs[i] = new PJob(i);
			jobs[i].setDuration(p[i]);
			jobs[i].setReleaseDate(rd[i]);
			jobs[i].setDueDate(dd[i]);
			jobs[i].resetSchedule();
		}
		jobs[0].addSuccessor(jobs[3]);
		jobs[2].addSuccessor(jobs[1]);
		return jobs;
	}

	private static void checkPermutation(int[] permutation, AbstractJob[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			Assert.assertEquals("check perm. index "+i, permutation[i], jobs[i].getID());
		}
	}

	@Test
	public void testSorting() {
		AbstractJob[] jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getShortestProcessingTime());
		checkPermutation(new int[] {3, 1, 4, 2, 0}, jobs);

		jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getEarliestDueDate());
		checkPermutation(new int[] {1, 4, 3, 0, 2}, jobs);

		jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getEarliestReleaseDate());
		checkPermutation(new int[] {0, 1, 2, 3, 4}, jobs);
	}


	@Test
	public void testPreemptiveSchedule() {
		PJob[] jobs = buildInstance();
		System.out.println(Arrays.toString(jobs));
		Pmtn1Scheduler.schedule1Lmax(jobs);
		ChocoChartFactory.createAndShowGUI("Test", ChocoChartFactory.createGanttChart("Test", jobs));
		System.out.println(Arrays.toString(jobs));
	}

	private final static void testJob(ITJob job, int start, int end) {
		assertTrue(job.isScheduled());
		assertTrue(job.isScheduledInTimeWindow());
		assertEquals(job.getEST(), start);
		assertEquals(job.getLCT(), end);
	}

	@Test
	public void testPreemptiveLmaxBug() {
		final int n = 3;
		ITJob[] jobs = new ITJob[n];
		for (int i = 0; i < n; i++) {
			jobs[i] = new PJob(i);
			jobs[i].setDuration(i+1);
		}
		jobs[0].setReleaseDate(0);
		jobs[1].setReleaseDate(1);
		jobs[2].setReleaseDate(3);

		jobs[0].setDueDate(1);
		jobs[1].setDueDate(2);
		jobs[2].setDueDate(2);

		jobs[0].addSuccessor(jobs[2]);
		jobs[1].addSuccessor(jobs[2]);
		
		Pmtn1Scheduler.schedule1Lmax(jobs);
		
		testJob(jobs[0], 0, 1);
		testJob(jobs[1], 1, 3);
		testJob(jobs[2], 3, 6);
		
	}

	@Test
	public void testSortTLinkedList() {
		TLinkedList<TLinkableInteger> list = new TLinkedList<TestCommon.TLinkableInteger>();
		list.add( new TLinkableInteger(6));
		list.add( new TLinkableInteger(5));
		list.add( new TLinkableInteger(8));
		list.add( new TLinkableInteger(2));
		list.add( new TLinkableInteger(1));
		list.add( new TLinkableInteger(7));
		list.add( new TLinkableInteger(3));
		list.add( new TLinkableInteger(5));
		TCollections.sort(list);

		TLinkableInteger current = list.getFirst(), next = list.getNext(current);
		while(next != null) {
			Assert.assertTrue(current.compareTo(next) <= 0);
			current = next;
			next = list.getNext(current);
		}
	}

	static class TLinkableInteger extends TLinkableAdapter implements Comparable<TLinkableInteger> {

		private static final long serialVersionUID = 8757623914472905181L;
		public final int value;


		public TLinkableInteger(int value) {
			super();
			this.value = value;
		}

		@Override
		public int compareTo(TLinkableInteger o) {
			return value - o.value;
		}

		@Override
		public String toString() {
			return Integer.toString(value);
		}


	}
}
