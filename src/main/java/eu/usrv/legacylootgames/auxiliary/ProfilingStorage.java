package eu.usrv.legacylootgames.auxiliary;


import java.util.*;


public class ProfilingStorage {
    private final Map<String, List<Long>> mProfilingMap;

    public ProfilingStorage() {
        mProfilingMap = new HashMap<String, List<Long>>();
    }

    public String[] getUniqueItems() {
        String[] tValues = null;
        mProfilingMap.keySet().toArray(tValues);
        return tValues;
    }

    /**
     * Add a new time to the list of pIdentifier. Will be ignored it tTotalTime == 0
     *
     * @param pIdentifier
     * @param pTotalTime
     */
    public void AddTimeToList(String pIdentifier, long pTotalTime) {
        try {
            if (pTotalTime == 0)
                return;

            if (!mProfilingMap.containsKey(pIdentifier))
                mProfilingMap.put(pIdentifier, new LinkedList<Long>());

            LinkedList<Long> ll = (LinkedList<Long>) mProfilingMap.get(pIdentifier);

            ll.addLast(pTotalTime);

            while (ll.size() > 50)
                ll.removeFirst();
        } catch (Exception e) {
            // Just do nothing. profiling is for debug purposes only anyways...
        }
    }

    /**
     * Return the average time required
     *
     * @param pIdentifier The Identifier in question
     * @return
     */
    public long GetAverageTime(String pIdentifier) {
        try {
            if (!mProfilingMap.containsKey(pIdentifier))
                return -1;

            int tTotalVal = 0;
            long tAverage = 0;
            long tReturnVal = 0;

            LinkedList ll = (LinkedList) mProfilingMap.get(pIdentifier);

            if (ll != null) {
                Iterator<Long> qItr = ll.iterator();
                while (qItr.hasNext()) {
                    tAverage += qItr.next();
                    tTotalVal++;
                }

                tReturnVal = (long) ((float) (tAverage / tTotalVal));
            }
            return tReturnVal;
        } catch (Exception e) {
            return -1;
        }
    }
}
