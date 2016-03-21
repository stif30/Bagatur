/*
 *  BagaturChess (UCI chess engine and tools)
 *  Copyright (C) 2005 Krasimir I. Topchiyski (k_topchiyski@yahoo.com)
 *  
 *  Open Source project location: http://sourceforge.net/projects/bagaturchess/develop
 *  SVN repository https://bagaturchess.svn.sourceforge.net/svnroot/bagaturchess
 *
 *  This file is part of BagaturChess program.
 * 
 *  BagaturChess is open software: you can redistribute it and/or modify
 *  it under the terms of the Eclipse Public License version 1.0 as published by
 *  the Eclipse Foundation.
 *
 *  BagaturChess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Eclipse Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License version 1.0
 *  along with BagaturChess. If not, see <http://www.eclipse.org/legal/epl-v10.html/>.
 *
 */
package bagaturchess.search.impl.env;


import bagaturchess.bitboard.api.PawnsEvalCache;
import bagaturchess.egtb.gaviota.GTBProbing;
import bagaturchess.egtb.gaviota.GTBProbing_NativeWrapper;
import bagaturchess.opening.api.OpeningBook;
import bagaturchess.search.api.IEvaluatorFactory;
import bagaturchess.search.api.IRootSearchConfig;
import bagaturchess.search.api.ISearchConfig_AB;
import bagaturchess.search.impl.evalcache.IEvalCache;
import bagaturchess.search.impl.pv.PVHistory;
import bagaturchess.search.impl.tpt.TPTable;
import bagaturchess.uci.api.IChannel;


public class SharedData {
	
	
	private PVHistory pvs_history;
	private IEvaluatorFactory evaluatorFactory;
	private IRootSearchConfig engineConfiguration;
	private ISearchConfig_AB searchConfig;
	private MemoryConsumers memoryConsumers;
	private GTBProbing gtb_probing;
	
	
	public SharedData(IChannel _channel, IRootSearchConfig _engineConfiguration) {
		this(_engineConfiguration, new MemoryConsumers(_channel, _engineConfiguration, false));
	}
	
	
	public SharedData(IRootSearchConfig _engineConfiguration, MemoryConsumers _memoryConsumers) {
		
		init(_engineConfiguration);

		setMemoryConsumers(_memoryConsumers);
	}
	
	
	private void init(IRootSearchConfig _engineConfiguration) {
		engineConfiguration = _engineConfiguration;
		searchConfig = engineConfiguration.getSearchConfig();
		
		try {
			String className = engineConfiguration.getEvalConfig().getEvaluatorFactoryClassName();
			evaluatorFactory = (IEvaluatorFactory) SharedData.class.getClassLoader().loadClass(className).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		
		
		pvs_history = new PVHistory();
	}
	
	
	public void setMemoryConsumers(MemoryConsumers _memoryConsumers) {
		
		memoryConsumers = _memoryConsumers;
		
		if (memoryConsumers != null) {
			if (GTBProbing_NativeWrapper.getInstance() != null) {
				gtb_probing = new GTBProbing(memoryConsumers.getGTBCache_OUT(), memoryConsumers.getGTBCache_IN());
			}
		}
	}
	
	
	public IEvaluatorFactory getEvaluatorFactory() {
		return evaluatorFactory;
	}
	
	public TPTable getTPT() {
		return memoryConsumers.getTPT();
	}
	
	public GTBProbing getGTBProbing() {
		return gtb_probing;
	}
	
	
	public PawnsEvalCache getPawnsCache() {
		return memoryConsumers.getPawnsCache().remove(0);
	}

	public PVHistory getPVs() {
		return pvs_history;
	}

	public IEvalCache getEvalCache() {
		return memoryConsumers.getEvalCache().remove(0);
	}

	@Override
	public String toString() {
		String msg = "TPT HIT RATE is: " + getTPT().getHitRate();
		return msg;
	}
	
	public void clear() {
		//history_check.clear();
		//history_all.clear();
		//pvs_history.clear();
		
		if (gtb_probing != null) gtb_probing.clear(); 
		
		memoryConsumers.clear();
	}

	public IRootSearchConfig getEngineConfiguration() {
		return engineConfiguration;
	}
	
	public ISearchConfig_AB getSearchConfig() {
		return searchConfig;
	}
	
	public OpeningBook getOpeningBook() {
		return memoryConsumers.getOpeningBook();
	}
}
