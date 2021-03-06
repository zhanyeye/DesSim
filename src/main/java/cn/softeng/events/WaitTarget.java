/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.softeng.events;

/**
 * 用于将一个线程封装到target(命令对象中)，包装进Event中，延迟执行
 * 一般创建一个组合waitTarget的Event后，会捕获(wait)被封装的线程
 */
class WaitTarget extends ProcessTarget {
	private Process proc;

	WaitTarget(Process p) {
		proc = p;
	}

	@Override
	Process getProcess() { return proc; }

	@Override
	void kill() {
		proc.kill();
	}

	@Override
	public String getDescription() {
		return "Waiting";
	}

	@Override
	public void process() {}
}
