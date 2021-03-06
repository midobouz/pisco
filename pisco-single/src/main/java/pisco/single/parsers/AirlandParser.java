/**
 *  Copyright (c) 2011, Arnaud Malapert
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Arnaud Malapert nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.single.parsers;


import java.util.Arrays;

import parser.absconparseur.tools.UnsupportedConstraintException;
import pisco.common.ITJob;
import pisco.common.PJob;
import choco.kernel.common.util.tools.MathUtils;

public class AirlandParser extends Abstract1MachineParser {


	public boolean hasRealPenalties;

	
	@Override
	public boolean hasSetupTimes() {
		return true;
	}

	
	@Override
	public boolean hasDeadlines() {
		return true;
	}


	@Override
	public void parse(boolean displayInstance)
			throws UnsupportedConstraintException {
		nbJobs = nextInt();
		freezeTime = nextInt(); // useless freeze time (for online algorithms
		appearanceDates= new int[nbJobs];
		jobs = new ITJob[nbJobs];
		final double[] _earlinessPenalties = new double[nbJobs];
		final double[] _tardinessPenalties = new double[nbJobs];
		setupTimes = new int[nbJobs][nbJobs];
		final int[] minSetupTimes = new int[nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			appearanceDates[i] = nextInt();  
			jobs[i] = new PJob(i);
			jobs[i].setReleaseDate(nextInt());
			jobs[i].setDueDate(nextInt());
			jobs[i].setDeadline(nextInt());
			// TODO - convert from double to int - created 11 mars 2012 by A. Malapert
			_earlinessPenalties[i] = nextDouble(); 
			_tardinessPenalties[i] = nextDouble();
			minSetupTimes[i]=Integer.MAX_VALUE;
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] = nextInt();
				if(minSetupTimes[i] > setupTimes[i][j]) {
					minSetupTimes[i]=setupTimes[i][j];
				}
			}
		}
		close();
		// Preprocess Data
		preprocessProcessingTimes(minSetupTimes);
		preprocessPenalties(_earlinessPenalties, _tardinessPenalties);
			if(displayInstance) {
			LOGGER.info(Arrays.toString(jobs));
		}
	}

	private final void preprocessProcessingTimes(final int[] minSetupTimes) {
		for (int i = 0; i < nbJobs; i++) {
			jobs[i].setDuration(minSetupTimes[i]);
			jobs[i].setDueDate( jobs[i].getDueDate() + minSetupTimes[i]);
			jobs[i].setDeadline( jobs[i].getDeadline() + minSetupTimes[i]);
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] -= minSetupTimes[i];
			}
		}
	}

	private final void preprocessPenalties(double[] _earlinessPenalties, double[] _tardinessPenalties) {
		int i = 0;
		while( i < nbJobs 
				&& MathUtils.isInt(_earlinessPenalties[i]) 
				&& MathUtils.isInt(_tardinessPenalties[i])) {
			i++;
		}
		hasRealPenalties = (i == nbJobs);
		int coefficient = hasRealPenalties ? 100 : 1;
		//Approximate penalties from double to int
		for (int j = 0; j < nbJobs; j++) {
			earlinessPenalties[j] = (int) Math.round( _earlinessPenalties[j] * coefficient);
			tardinessPenalties[j] = (int) Math.round( _earlinessPenalties[j] * coefficient);
			jobs[i].setWeight(earlinessPenalties[i]);
		}	
	}
	
	public final String getParserMsg() {
		StringBuilder b = new StringBuilder(super.getParserMsg());
		if(hasRealPenalties) {
			b.append("REAL PENALTIES   ");
		}
		return b.toString();
	}

}