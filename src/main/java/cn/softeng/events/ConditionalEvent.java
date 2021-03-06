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
 * 条件事件，组合了一个条件对象，在仿真运行时，用于检查一些情况，例如：用户暂停仿真
 */
final class ConditionalEvent extends BaseEvent {
	Conditional c;

	ConditionalEvent(Conditional c, ProcessTarget t, EventHandle hand) {
		this.target = t;
		this.handle = hand;
		this.c = c;
	}
}
