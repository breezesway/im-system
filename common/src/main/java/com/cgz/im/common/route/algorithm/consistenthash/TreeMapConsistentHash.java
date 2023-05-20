package com.cgz.im.common.route.algorithm.consistenthash;

import com.cgz.im.common.enums.UserErrorCode;
import com.cgz.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性哈希实现：TreeMap
 */
public class TreeMapConsistentHash extends AbstractConsistentHash{

    private static final int NODE_SIZE = 2;

    /**
     * 每一个真实节点所含有的虚拟节点数量
     */
    private TreeMap<Long,String> treeMap = new TreeMap<>();

    /**
     * 每一个人真实节点对应一致性哈希环中的1+NODE_SIZE个节点
     * @param key
     * @param value
     */
    @Override
    protected void add(long key, String value) {
        for(int i = 0;i<NODE_SIZE;i++){
            treeMap.put(super.hash("node"+key+i),value);
        }
        treeMap.put(super.hash(key+""),value);
    }

    @Override
    protected String getFirstNodeValue(String key) {
        Long hash = super.hash(key);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if(!last.isEmpty()){
            return last.get(last.firstKey());
        }
        if(treeMap.size() == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }
}
