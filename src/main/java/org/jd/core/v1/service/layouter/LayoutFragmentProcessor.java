/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter;

import static org.jd.core.v1.api.Printer.UNKNOWN_LINE_NUMBER;

import java.util.List;
import java.util.Map;

import org.jd.core.v1.api.Processor;
import org.jd.core.v1.model.fragment.FixedFragment;
import org.jd.core.v1.model.fragment.FlexibleFragment;
import org.jd.core.v1.model.fragment.Fragment;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.service.layouter.model.Section;
import org.jd.core.v1.service.layouter.util.VisitorsHolder;
import org.jd.core.v1.service.layouter.visitor.BuildSectionsVisitor;
import org.jd.core.v1.service.layouter.visitor.UpdateSpacerBetweenMovableBlocksVisitor;

/**
 * Layout (compact, expend, move) a list of fragments.<br>
 * <br>
 *
 * Input: List<{@link Fragment}><br>
 * Output: List<{@link Fragment}><br>
 */
public class LayoutFragmentProcessor implements Processor {

	/**
	 * Layout (compact, expend, move) a list of fragments. Update fragments in place
	 */
	@Override
	public void process(Message message) {
		int maxLineNumber = message.getMaxLineNumber(UNKNOWN_LINE_NUMBER);
		boolean containsByteCode = message.getContainsByteCode(Boolean.FALSE);
		boolean showBridgeAndSynthetic = message.getShowBridgeAndSynthetic(Boolean.FALSE);
		Map<String, Object> configuration = message.getConfiguration();
		Object realignLineNumbersConfiguration = (configuration == null) ? "false"
				: configuration.get("realignLineNumbers");
		boolean realignLineNumbers = (realignLineNumbersConfiguration == null) ? false
				: "true".equals(realignLineNumbersConfiguration.toString());

		List<Fragment> fragments = message.getFragments();

		if ((maxLineNumber != UNKNOWN_LINE_NUMBER) && !containsByteCode && !showBridgeAndSynthetic
				&& realignLineNumbers) {
			BuildSectionsVisitor buildSectionsVisitor = new BuildSectionsVisitor();

			// Create sections
			for (Fragment fragment : fragments) {
				fragment.accept(buildSectionsVisitor);
			}

			List<Section> sections = buildSectionsVisitor.getSections();
			VisitorsHolder holder = new VisitorsHolder();
			UpdateSpacerBetweenMovableBlocksVisitor visitor = new UpdateSpacerBetweenMovableBlocksVisitor();

			// Try to release constraints twice for each section
			int sumOfRates = Integer.MAX_VALUE;
			int max = sections.size() * 2;

			if (max > 20) {
				max = 20;
			}

			for (int loop = 0; loop < max; loop++) {
				// Update spacers
				visitor.reset();

				for (Section section : sections) {
					for (FlexibleFragment fragment : section.getFlexibleFragments()) {
						fragment.accept(visitor);
					}
					if (section.getFixedFragment() != null) {
						section.getFixedFragment().accept(visitor);
					}
				}

				// Layout sections
				for (int redo = 0; redo < 10; redo++) {
					boolean changed = false;

					for (Section section : sections) {
						changed |= section.layout(false);
					}
					if (changed == false) {
						// Nothing changed -> Quit loop
						break;
					}
				}

				// Update the ratings
				int newSumOfRates = 0;
				Section mostConstrainedSection = sections.get(0);

				for (Section section : sections) {
					section.updateRate();

					if (mostConstrainedSection.getRate() < section.getRate()) {
						mostConstrainedSection = section;
					}

					newSumOfRates += section.getRate();
				}

				// Move fragments from the most constrained section
				if (mostConstrainedSection.getRate() == 0) {
					// No more constrained section -> Quit loop
					break;
				}

				if (sumOfRates > newSumOfRates) {
					sumOfRates = newSumOfRates;
				} else {
					// The sum of the constraints does not decrease -> Quit loop
					break;
				}

				if (!mostConstrainedSection.releaseConstraints(holder)) {
					break;
				}
			}

			// Force layout
			for (Section section : sections) {
				section.layout(true);
			}

			// Update fragments
			fragments.clear();

			// FIXME should output JavaFragment's only ?!?
			for (Section section : sections) {
				fragments.addAll(section.getFlexibleFragments());

				FixedFragment fixedFragment = section.getFixedFragment();

				if (fixedFragment != null) {
					fragments.add(fixedFragment);
				}
			}
		}
	}

	private static LayoutFragmentProcessor instance = null;

	/**
	 * Get Singleton instance
	 */
	public static LayoutFragmentProcessor getInstance() {
		if (instance == null) {
			instance = new LayoutFragmentProcessor();
		}
		return instance;
	}
}
