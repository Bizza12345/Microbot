/*
 * Copyright (c) 2023, jLereback <https://github.com/jLereback>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.skills.agility;

import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.steps.ConditionalStep;
import net.runelite.client.plugins.microbot.questhelper.steps.DetailedQuestStep;
import net.runelite.client.plugins.microbot.questhelper.steps.ObjectStep;
import net.runelite.client.plugins.microbot.questhelper.steps.QuestStep;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;

import java.util.Arrays;
import java.util.Collections;

public class AlKharid extends AgilityCourse
{
	QuestStep alKharidSidebar;
	QuestStep climbRoughWall, walkFirstRope, swingCable, gripZipLine, swingTree, climbBeams, walkSecondRope, jumpGap;
	Zone firstRopeZone, cableZone, zipLineZone, treeZone, beamsZone, secondRopeZone, gapZone;
	ZoneRequirement inFirstRopeZone, inCableZone, inZipLineZone, inTreeZone, inBeamsZone, inSecondRopeZone, inGapZone;

	ConditionalStep alKharidStep;
	PanelDetails alKharidPanels;

	public AlKharid(QuestHelper questHelper)
	{
		super(questHelper);
	}

	@Override
	protected ConditionalStep loadStep()
	{
		setupZones();
		setupConditions();
		setupSteps();
		addSteps();

		return alKharidStep;
	}

	@Override
	protected void setupConditions()
	{
		inFirstRopeZone = new ZoneRequirement(firstRopeZone);
		inCableZone = new ZoneRequirement(cableZone);
		inZipLineZone = new ZoneRequirement(zipLineZone);
		inTreeZone = new ZoneRequirement(treeZone);
		inBeamsZone = new ZoneRequirement(beamsZone);
		inSecondRopeZone = new ZoneRequirement(secondRopeZone);
		inGapZone = new ZoneRequirement(gapZone);
	}

	@Override
	protected void setupZones()
	{
		firstRopeZone = new Zone(new WorldPoint(3272, 3174, 3), new WorldPoint(3278, 3192, 3));
		cableZone = new Zone(new WorldPoint(3265, 3161, 3), new WorldPoint(3272, 3173, 3));
		zipLineZone = new Zone(new WorldPoint(3283, 3160, 3), new WorldPoint(3312, 3176, 1));
		treeZone = new Zone(new WorldPoint(3313, 3160, 1), new WorldPoint(3318, 3172, 2));
		beamsZone = new Zone(new WorldPoint(3312, 3173, 2), new WorldPoint(3318, 3179, 3));
		secondRopeZone = new Zone(new WorldPoint(3307, 3180, 3), new WorldPoint(3318, 3186, 3));
		gapZone = new Zone(new WorldPoint(3297, 3185, 3), new WorldPoint(3306, 3194, 3));
	}

	@Override
	protected void setupSteps()
	{
		//Al Kharid obstacles
		climbRoughWall = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_WALLCLIMB, new WorldPoint(3273, 3195, 0),
			"Climb the rough wall just southeast of the Gnome Glider in Al Kharid.",
			Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		walkFirstRope = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_TIGHTROPE_1, new WorldPoint(3272, 3181, 3),
			"Cross the tightrope.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		swingCable = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_ROPE_SWING, new WorldPoint(3269, 3166, 3),
			"Swing across the cable.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		gripZipLine = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_SLIDE_SIDE, new WorldPoint(3303, 3163, 3),
			"Teeth-grip the zip line.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		swingTree = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_BAMBOO_TREE_TOP, new WorldPoint(3318, 3166, 1),
			"Swing across the tropical tree.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		climbBeams = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_WALLCLIMB_2, new WorldPoint(3316, 3179, 2),
			"Climb the roof top beams.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		walkSecondRope = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_TIGHTROPE_4, new WorldPoint(3313, 3186, 3),
			"Cross the tightrope.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));

		jumpGap = new ObjectStep(this.questHelper, ObjectID.ROOFTOPS_KHARID_LEAPDOWN, new WorldPoint(3300, 3193, 3),
			"Jump down the gap.", Collections.EMPTY_LIST, Arrays.asList(recommendedItems));
	}

	@Override
	protected void addSteps()
	{
		alKharidStep = new ConditionalStep(this.questHelper, climbRoughWall);
		alKharidStep.addStep(inFirstRopeZone, walkFirstRope);
		alKharidStep.addStep(inCableZone, swingCable);
		alKharidStep.addStep(inZipLineZone, gripZipLine);
		alKharidStep.addStep(inTreeZone, swingTree);
		alKharidStep.addStep(inBeamsZone, climbBeams);
		alKharidStep.addStep(inSecondRopeZone, walkSecondRope);
		alKharidStep.addStep(inGapZone, jumpGap);

		alKharidSidebar = new DetailedQuestStep(this.questHelper, "Train agility at the Al Kharid Rooftop Course, starting north of the Tannery.");
		alKharidSidebar.addSubSteps( climbRoughWall, walkFirstRope, swingCable, gripZipLine, swingTree, climbBeams, walkSecondRope, jumpGap, alKharidStep);
	}

	@Override
	protected PanelDetails getPanelDetails()
	{
		alKharidPanels = new PanelDetails("20 - 30: Al Kharid", Collections.singletonList(alKharidSidebar)
		);
		return alKharidPanels;
	}
}
