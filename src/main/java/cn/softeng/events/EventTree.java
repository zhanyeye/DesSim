package cn.softeng.events;

import java.util.Arrays;

/**
 * @date: 11/4/2020 10:18 AM
 * @Description:
 * 是一个红黑树实现，旨在实现存储事件的优先队列
 * 每一个红黑树结点都都对应一个事件链表（链表中的事件时间和优先级都一样）
 */
class EventTree {

    /**
     * 根结点
     */
    private EventNode root = EventNode.nilNode;
    /**
     * 最小值结点，（红黑树最左子节点）
     */
    private EventNode lowest = null;


    /**
     * Scratch space, used instead of having parent pointers
     * 暂存空间，用来暂存父指针
     */
    private EventNode[] scratch = new EventNode[64];
    private int scratchPos = 0;

    /**
     * 将结点放到暂存空间中
     * @param n
     */
    private void pushScratch(EventNode n) {
        scratch[scratchPos++] = n;
    }

    /**
     * 从后向前删除n个暂存结点
     * @param n
     */
    private void dropScratch(int n) {
        scratchPos = Math.max(0, scratchPos - n);
    }


    /**
     * Get the 'nth' node from the end of the scratch (1 being the first)
     * 从scratch中，取出倒数第n个结点
     * @param n 倒数第几个节点
     * @return
     */
    private EventNode getScratch(int n) {
        return (scratchPos >= n) ? scratch[scratchPos - n] : null;
    }


    /**
     * 重置暂存空间（将它的索引置0）
     */
    private void resetScratch() {
        scratchPos = 0;
    }

    /**
     * 获取事件优先队列的队首元素（返回红黑树中最小值结点）
     * @return
     */
    EventNode getNextNode() {
        if (lowest == null) {
            updateLowest();
        }
        return lowest;
    }

    /**
     * 重置红黑树
     */
    final void reset() {
        root = EventNode.nilNode;
        lowest = null;
        clearFreeList();
        resetScratch();
        Arrays.fill(scratch, null);
    }

    /**
     * 更新红黑树的最小值结点，被 getNextNode() 调用
     * 即遍历找到红黑树中最小结点
     */
    private void updateLowest() {
        if (root == EventNode.nilNode) {
            lowest = null;
            return;
        }
        EventNode current = root;
        while (current.left != EventNode.nilNode) {
            current = current.left;
        }

        lowest = current;
    }

    /**
     * 根据调度刻度和优先级去查询结点，若没有则创建一个
     * @param schedTick 发生时间
     * @param priority  优先级
     * @return
     */
    final EventNode createOrFindNode(long schedTick, int priority) {

        // 若红黑树为空，则新建一个节点作为根节点
        if (root == EventNode.nilNode) {
            root = getNewNode(schedTick, priority);
            lowest = root;
            return root;
        }
        resetScratch();

        EventNode n = root;
        EventNode newNode = null;

        // 遍历整颗树，去寻找目标节点，
        while (true) {
            int comp = n.compare(schedTick, priority);
            if (comp == 0) {
                // Found existing node
                return n;
            }
            EventNode next = comp > 0 ? n.left : n.right;
            if (next != EventNode.nilNode) {
                // 若节点n的next不为空，将n保存到Scratch中， n指向next,继续遍历
                pushScratch(n);
                n = next;
                continue;
            }

            // There is no current node for this time/priority
            newNode = getNewNode(schedTick, priority);
            pushScratch(n);
            newNode.red = true;
            if (comp > 0) {
                n.left = newNode;
            } else {
                n.right = newNode;
            }
            break;
        }

        // 针对新节点平衡红黑树
        insertBalance(newNode);
        root.red = false;

        if (lowest != null && newNode.compareToNode(lowest) < 0) {
            // 更新红黑树的最小节点
            lowest = newNode;
        }
        return newNode;

    }

    /**
     * 针对新插入的节点平衡红黑树
     * @param n
     */
    private void insertBalance(EventNode n) {
        // See the wikipedia page for red-black trees to understand the case numbers

        // 获取该节点的父节点
        EventNode parent = getScratch(1);
        if (parent == null || !parent.red) {
            // cases 1 and 2: 没有父节点和父节点为黑色的情况
            return;
        }

        // 获取祖父节点
        EventNode gp = getScratch(2);
        if (gp == null) {
            // 没有祖父节点，父节点为根节点的情况（即父节点为黑色）
            return;
        }

        // 获取叔叔节点
        EventNode uncle = (gp.left == parent ? gp.right : gp.left);

        if (uncle.red) {
            // Both parent and uncle are red
            // case 3：
            // 1. 新增当前节点，默认颜色为红色;
            // 2. 父节点变成黑色
            // 3. 叔叔节点U变成黑色
            // 4. 祖父节点变成红色
            // 5. 如果祖父节点变红不满足红黑树性质，则将祖父节点当成新增节点递归处理
            parent.red = false;
            uncle.red = false;
            gp.red = true;
            dropScratch(2);
            insertBalance(gp);
            return;
        }

        // case 4

        if (n == parent.right && gp != null && parent == gp.left) {
            // Right child of a left parent, rotate left at parent
            parent.rotateLeft(gp);
            parent = n;
            n = n.left;
        }
        else if (n == parent.left && gp != null && parent == gp.right) {
            // left child of right parent, rotate right at parent
            parent.rotateRight(gp);
            parent = n;
            n = n.right;
        }

        EventNode ggp = getScratch(3);
        // case 5
        gp.red = true;
        parent.red = false;
        if (parent.left == n) {
            if (gp == root)
                root = gp.left;
            gp.rotateRight(ggp);
        } else {
            if (gp == root)
                root = gp.right;
            gp.rotateLeft(ggp);
        }

    }


    /**
     * 删除红黑树的指定节点
     * @param schedTick
     * @param priority
     * @return
     */
    final boolean removeNode(long schedTick, int priority) {
        // First find the node to remove
        resetScratch();
        lowest = null;

        EventNode current = root;
        while (true) {
            int comp = current.compare(schedTick, priority);

            if (comp == 0) break;

            pushScratch(current);
            if (comp > 0)
                current = current.left;
            else
                current = current.right;
            if (current == EventNode.nilNode) {
                return false; // Node not found
            }
        }

        // Debugging
        // 如果节点所指向的链表仍然有元素，则抛出异常
        if (current.head != null || current.tail != null)
            throw new RuntimeException("Removing non-empy node");

        // We have the node to remove
        if (current.left != EventNode.nilNode && current.right != EventNode.nilNode) {
            current = swapToLeaf(current);
        }

//		// Verify we have a proper parent list (testing only)
//		if (scratchPos > 0 && scratch[0] != root) throw new RuntimeException("Bad parent list");
//		for (int i = 1; i < scratchPos; ++i) {
//			// Check the current node is a child of the previous
//			EventNode child = scratch[i];
//			EventNode parent = scratch[i-1];
//			if (parent.left != child && parent.right != child) {
//				throw new RuntimeException("Bad parent list");
//			}
//		}

        EventNode child = current.left != EventNode.nilNode ? current.left : current.right;

        EventNode parent = getScratch(1);

        // Drop the node
        if (parent != null) {
            if (parent.left == current) {
                parent.left = child;
            } else {
                parent.right = child;
            }
        }

        if (current == root) {
            root = child;
        }

        boolean currentIsRed = current.red;

        reuseNode(current);

        if (currentIsRed) {
            return true; // We swapped out a red node, there's nothing else to do
        }
        if (child.red) {
            child.red = false;
            return true; // traded a red for a black, still all good.
        }

        // We removed a black node with a black child, we need to re-balance the tree
        deleteBalance(child);
        root.red = false;
        return true;
    }

    /**
     * 将该节点和叶子节点进行交换，删除节点时可能调用
     * @param node
     * @return
     */
    private EventNode swapToLeaf(EventNode node) {
        pushScratch(node);
        EventNode curr = node.left;
        while (curr.right != EventNode.nilNode) {
            pushScratch(curr);
            curr = curr.right;
        }
        node.cloneFrom(curr);
        return curr;
    }

    /**
     * 删除节点后的平衡操作
     * @param n
     */
    private void deleteBalance(EventNode n) {
        // At all times the scratch space should contain the parent list (but not n)
        EventNode parent = getScratch(1);
        if (parent == null)
            return;

        EventNode sib = (parent.left == n) ? parent.right : parent.left;
        EventNode gp = getScratch(2);

        // case 2
        if (sib.red) {
            sib.red = false;
            parent.red = true;
            if (n == parent.left)
                parent.rotateLeft(gp);
            else
                parent.rotateRight(gp);
            if (root == parent)
                root = sib;

            // update the parent list after the rotation
            dropScratch(1);
            pushScratch(sib);
            pushScratch(parent);
            gp = getScratch(2);

            // update the sibling
            sib = (parent.left == n) ? parent.right : parent.left;
        }

        // case 3
        if (!parent.red && !sib.left.red && !sib.right.red) {
            sib.red = true;
            dropScratch(1);
            deleteBalance(parent);
            return;
        }

        // case 4
        if (parent.red && !sib.left.red && !sib.right.red) {
            parent.red = false;
            sib.red = true;
            return;
        }

        // case 5
        if (parent.left == n &&
                !sib.right.red &&
                sib.left.red) {

            sib.red = true;
            sib.left.red = false;
            sib.rotateRight(parent);

            sib = parent.right;
        } else if (parent.right == n &&
                !sib.left.red &&
                sib.right.red) {

            sib.red = true;
            sib.right.red = false;
            sib.rotateLeft(parent);

            sib = parent.left;
        }

        // case 6
        sib.red = parent.red;
        parent.red = false;
        if (n == parent.left) {
            sib.right.red = false;
            parent.rotateLeft(gp);
        } else {
            sib.left.red = false;
            parent.rotateRight(gp);
        }
        if (root == parent) {
            root = sib;
        }
    }

    /**
     * 运行整棵树的所有runner
     * @param runner
     */
    final void runOnAllNodes(EventNode.Runner runner) {
        runOnNode(root, runner);
    }

    /**
     * 运行某个节点所有子树的所有runner
     * @param node
     * @param runner
     */
    private void runOnNode(EventNode node, EventNode.Runner runner) {
        if (node == EventNode.nilNode) {
            return;
        }

        runOnNode(node.left, runner);

        runner.runOnNode(node);

        runOnNode(node.right, runner);
    }



    /**
     * 可复用空闲节点链表，是一个只用到节点左子树的链表结构
     */
    private EventNode freeList = null;

    /**
     * 根据调度刻度和事件优先级创建一个新节点
     * 在创建是考虑复用空闲节点
     * @param schedTick
     * @param priority
     * @return
     */
    private EventNode getNewNode(long schedTick, int priority) {
        // 是否有可复用的空闲节点
        if (freeList == null) {
            return new EventNode(schedTick, priority);
        }

        // 复用空闲节点的头节点
        EventNode ret = freeList;
        // 重置空闲节点为头节点的下一个节点
        freeList = freeList.left;

        ret.schedTick = schedTick;
        ret.priority = priority;
        ret.head = null;
        ret.tail = null;
        ret.left = EventNode.nilNode;
        ret.right = EventNode.nilNode;
        ret.red = false;

        return ret;
    }

    /**
     * 回收空闲的节点，
     * @param node
     */
    private void reuseNode(EventNode node) {
        // Clear the node
        node.left = null;
        node.right = null;
        node.head = null;
        node.tail = null;

        node.left = freeList;
        freeList = node;
    }

    /**
     * 清空空闲节点
     */
    private void clearFreeList() {
        freeList = null;
    }



    // ******************
    // 测试验证红黑树相关代码
    // ******************



    // Verify the sorting structure and return the number of nodes
    final int verify() {
        if (root == EventNode.nilNode) {
            return 0;
        }

        if (EventNode.nilNode.red == true)
            throw new RuntimeException("nil node corrupted, turned red");
        return verifyNode(root);
    }

    private int verifyNode(EventNode n) {
        int lBlacks = 0;
        int rBlacks = 0;

        if (n.left != EventNode.nilNode) {
            if (n.compareToNode(n.left) != 1)
                throw new RuntimeException("RB tree order verify failed");
            lBlacks = verifyNode(n.left);
        }
        if (n.right != EventNode.nilNode) {
            if (n.compareToNode(n.right) != -1)
                throw new RuntimeException("RB tree order verify failed");
            rBlacks = verifyNode(n.right);
        }

        if (n.red) {
            if (n.left.red)
                throw new RuntimeException("RB tree red-red child verify failed");
            if (n.right.red)
                throw new RuntimeException("RB tree red-red child verify failed");
        }

        if (lBlacks != rBlacks)
            throw new RuntimeException("RB depth equality verify failed");
        return lBlacks + (n.red ? 0 : 1);
    }

    // Search the tree and return true if this node is found
    final EventNode find(long schedTick, int priority) {
        EventNode curr = root;
        while (true) {
            if (curr == EventNode.nilNode) return null;
            int comp = curr.compare(schedTick, priority);
            if (comp == 0) {
                return curr;
            }
            if (comp < 0) {
                curr = curr.right;
                continue;
            }
            curr = curr.left;
            continue;
        }
    }

    final int verifyNodeCount() {
        if (root == EventNode.nilNode) return 0;
        return countNodes(root);
    }


    private int countNodes(EventNode n) {
        int count = 1;
        if (n.left != EventNode.nilNode) {
            count += countNodes(n.left);
        }
        if (n.right != EventNode.nilNode) {
            count += countNodes(n.right);
        }

        return count;
    }

}