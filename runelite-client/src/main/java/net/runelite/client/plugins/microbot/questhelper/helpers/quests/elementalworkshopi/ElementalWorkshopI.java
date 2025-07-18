/*
 * Copyright (c) 2021, Zoinkwiz
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

package net.runelite.client.plugins.microbot.questhelper.helpers.quests.elementalworkshopi;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.collections.KeyringCollection;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.ComplexStateQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.NpcCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemOnTileRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.KeyringRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarplayerRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarPlayerID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ElementalWorkshopI extends ComplexStateQuestHelper
{
	//Items Required
	ItemRequirement knife, pickaxe, needle, thread, leather, hammer, coal4, combatGear, batteredBook, elementalOre,
		elementalBar, batteredKey, stoneBowlHighlighted, lavaBowlHighlighted, slashedBook;

	QuestStep searchBookcase, readBook, useKnifeOnBook, openOddWall, goDownStairs,
		turnEastControl, turnWestControl, pullLever, searchLeatherCrate, searchNeedleCrate, fixBellows,
		pullBellowsLever, getStoneBowl, useBowlOnLava, useLavaOnFurnace, mineRock, killRock, pickUpOre,
		forgeBar, smithShield;

	Requirement inWorkshop, inStairwell, hasSlashedBook, hasReadBook, enteredWall, foundLeather,
		turnedValve1, turnedValve2, solvedWater, hasLeatherOrSearched, elementalOreNearby, earthNearby,
		solvedAir, solvedFire, fixedBellow;

	//Zones
	Zone workshop, stairwell;

	@Override
	public QuestStep loadStep()
	{
		// TODO: To what degree should this quest make use of PuzzleWrapper?
		initializeRequirements();
		setupConditions();
		setupSteps();

		ConditionalStep goReadBook = new ConditionalStep(this, searchBookcase);
		goReadBook.addStep(batteredBook, readBook);

		ConditionalStep enterElementalWorkshop = new ConditionalStep(this, searchBookcase);
		enterElementalWorkshop.addStep(inStairwell, goDownStairs);
		enterElementalWorkshop.addStep(batteredKey, openOddWall);
		enterElementalWorkshop.addStep(batteredBook, useKnifeOnBook);

		ConditionalStep goSolveWater = new ConditionalStep(this, openOddWall);
		goSolveWater.addStep(new Conditions(inWorkshop, turnedValve1, turnedValve2), pullLever);
		goSolveWater.addStep(new Conditions(inWorkshop, turnedValve1), turnWestControl);
		goSolveWater.addStep(inWorkshop, turnEastControl);
		goSolveWater.addStep(inStairwell, goDownStairs);

		ConditionalStep goSolveAir = new ConditionalStep(this, openOddWall);
		goSolveAir.addStep(new Conditions(needle, hasLeatherOrSearched, fixedBellow, inWorkshop), pullBellowsLever);
		goSolveAir.addStep(new Conditions(needle, hasLeatherOrSearched, inWorkshop), fixBellows);
		goSolveAir.addStep(new Conditions(needle, inWorkshop), searchLeatherCrate);
		goSolveAir.addStep(inWorkshop, searchNeedleCrate);
		goSolveAir.addStep(inStairwell, goDownStairs);

		ConditionalStep goSolveFire = new ConditionalStep(this, openOddWall);
		goSolveFire.addStep(new Conditions(lavaBowlHighlighted, inWorkshop), useLavaOnFurnace);
		goSolveFire.addStep(new Conditions(stoneBowlHighlighted, inWorkshop), useBowlOnLava);
		goSolveFire.addStep(inWorkshop, getStoneBowl);
		goSolveFire.addStep(inStairwell, goDownStairs);

		ConditionalStep goMakeShield = new ConditionalStep(this, openOddWall);
		goMakeShield.addStep(new Conditions(inWorkshop, elementalBar), smithShield);
		goMakeShield.addStep(new Conditions(inWorkshop, elementalOre), forgeBar);
		goMakeShield.addStep(new Conditions(elementalOreNearby), pickUpOre);
		goMakeShield.addStep(new Conditions(earthNearby), killRock);
		goMakeShield.addStep(inWorkshop, mineRock);
		goMakeShield.addStep(inStairwell, goDownStairs);

		ConditionalStep quest = new ConditionalStep(this, goReadBook);
		quest.addStep(new Conditions(solvedWater, solvedAir, solvedFire), goMakeShield);
		quest.addStep(new Conditions(solvedWater, solvedAir), goSolveFire);
		quest.addStep(solvedWater, goSolveAir);
		quest.addStep(enteredWall, goSolveWater);
		quest.addStep(hasReadBook, enterElementalWorkshop);

		return quest;
	}

	@Override
	protected void setupRequirements()
	{
		knife = new ItemRequirement("Knife", ItemID.KNIFE).isNotConsumed();
		knife.setHighlightInInventory(true);
		pickaxe = new ItemRequirement("Any pickaxe", ItemCollections.PICKAXES).isNotConsumed();
		needle = new ItemRequirement("Needle", ItemID.NEEDLE).isNotConsumed();
		needle.setTooltip("You can obtain this during the quest");
		thread = new ItemRequirement("Thread", ItemID.THREAD);
		leather = new ItemRequirement("Leather", ItemID.LEATHER);
		leather.setTooltip("You can obtain this during the quest");

		hammer = new ItemRequirement("Hammer", ItemCollections.HAMMER).isNotConsumed();
		coal4 = new ItemRequirement("Coal", ItemID.COAL, 4);

		combatGear = new ItemRequirement("Combat gear", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());
		batteredBook = new ItemRequirement("Battered book", ItemID.ELEMENTAL_WORKSHOP_SHIELD_BOOK);
		batteredBook.setHighlightInInventory(true);
		slashedBook = new ItemRequirement("Slashed book", ItemID.ELEMENTAL_WORKSHOP_SHIELD_BOOK_SLASHED);
		slashedBook.setTooltip("If you've lost it you can get another by searching the bookcase in the building south of " +
			"the odd wall");
		batteredKey = new KeyringRequirement("Battered Key", configManager, KeyringCollection.BATTERED_KEY);
		batteredKey.setTooltip("If you've lost it you can get another by searching the bookcase in the building south of " +
			"the odd wall");
		elementalOre = new ItemRequirement("Elemental ore", ItemID.ELEMENTAL_WORKSHOP_ORE);
		elementalOre.setHighlightInInventory(true);
		elementalBar = new ItemRequirement("Elemental bar", ItemID.ELEMENTAL_WORKSHOP_BAR);
		elementalBar.setHighlightInInventory(true);
		stoneBowlHighlighted = new ItemRequirement("A stone bowl", ItemID.ELEMENTAL_WORKSHOP_LAVA_BOWL);
		stoneBowlHighlighted.setHighlightInInventory(true);

		lavaBowlHighlighted = new ItemRequirement("A stone bowl", ItemID.ELEMENTAL_WORKSHOP_LAVA_BOWL_FULL);
		lavaBowlHighlighted.setHighlightInInventory(true);
	}

	public void setupSteps()
	{
		searchBookcase = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_BOOKCASE, new WorldPoint(2716, 3482, 0), "Search the " +
			"marked bookcase in Seers' Village.");
		readBook = new DetailedQuestStep(this, "Read the battered book.", batteredBook);
		readBook.addDialogStep("Yes.");
		useKnifeOnBook = new DetailedQuestStep(this, "Use a knife on the battered book.", knife, batteredBook);
		openOddWall = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_ODDWALL_L, new WorldPoint(2709, 3495, 0),
			"Open the odd wall to the north of the bookcase.", batteredKey, slashedBook);
		goDownStairs = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_SPIRALSTAIRSTOP, new WorldPoint(2711, 3498, 0),
			"Climb down the staircase.");

		turnEastControl = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_VALVE_1, new WorldPoint(2726, 9908, 0),
			"Turn the marked water control in the north room.");
		turnWestControl = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_VALVE_2, new WorldPoint(2713, 9908, 0),
			"Turn the other marked water control in the north room.");
		pullLever = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_WATER_LEVER, new WorldPoint(2722, 9906, 0),
			"Pull the lever in the north room.");
		searchLeatherCrate = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_BOX_1, new WorldPoint(2717, 9894, 0),
			"Search the marked crate for some leather.");
		searchNeedleCrate = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_BOX_2, new WorldPoint(2700, 9887, 0),
			"Search the stacked crates in the west room for a needle.");
		fixBellows = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_BELLOWS_MULTILOC, new WorldPoint(2735, 9884, 0), "" +
			"Repair the bellows in the east room.", needle, thread, leather);
		fixBellows.addSubSteps(searchLeatherCrate, searchNeedleCrate);
		pullBellowsLever = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_AIR_LEVER, new WorldPoint(2734, 9887, 0),
			"Pull the lever next to the bellows.");
		getStoneBowl = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_BOX_4, new WorldPoint(2724, 9894, 0),
			"Search the boxes in the north east of the central room.");
		useBowlOnLava = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_TROUGH_2, new WorldPoint(2717, 9871, 0),
			"Use the stone bowl on the lava trough in the south room.", stoneBowlHighlighted);
		useBowlOnLava.addIcon(ItemID.ELEMENTAL_WORKSHOP_LAVA_BOWL);
		useLavaOnFurnace = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_FURNACE, new WorldPoint(2726, 9875, 0),
			"Use the lava-filled bowl on the furnace.", lavaBowlHighlighted);
		useLavaOnFurnace.addIcon(ItemID.ELEMENTAL_WORKSHOP_LAVA_BOWL_FULL);
		mineRock = new NpcStep(this, NpcID.ELEM1_QIP_EARTH_ELEMENTAL_ROCK_VERSION_ROCK, new WorldPoint(2703, 9894, 0),
			"Mine one of the elemental rocks in the west room, ready to fight a level 35.", true, pickaxe);
		killRock = new NpcStep(this, NpcID.ELEM1_QIP_EARTH_ELEMENTAL_ROCK_VERSION, new WorldPoint(2703, 9897, 0),
			"Kill the rock elemental that appeared.");
		pickUpOre = new ItemStep(this, "Pick up the elemental ore.", elementalOre);
		forgeBar = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_FURNACE, new WorldPoint(2726, 9875, 0),
			"Use the elemental ore on the furnace in the south room.", elementalOre, coal4);
		forgeBar.addIcon(ItemID.ELEMENTAL_WORKSHOP_ORE);
		smithShield = new ObjectStep(this, ObjectID.ELEMENTAL_WORKSHOP_WORKBENCH, new WorldPoint(2717, 9888, 0),
			"Use the bar on one of the workbenches in the central room to complete the quest.", elementalBar,
			hammer, slashedBook);
		smithShield.addIcon(ItemID.ELEMENTAL_WORKSHOP_BAR);
	}

	public void setupConditions()
	{
		inStairwell = new ZoneRequirement(stairwell);
		inWorkshop = new ZoneRequirement(workshop);

		hasSlashedBook = new VarbitRequirement(2057, 1);
		hasReadBook = new VarplayerRequirement(VarPlayerID.ELEMENTAL_WORKSHOP_BITS, true, 1);
		enteredWall = new VarplayerRequirement(VarPlayerID.ELEMENTAL_WORKSHOP_BITS, true, 15);
		foundLeather = new VarbitRequirement(2066, 1);
		turnedValve1 = new VarbitRequirement(2059, 1);
		turnedValve2 = new VarbitRequirement(2058, 1);
		solvedWater = new VarbitRequirement(2060, 1);

		hasLeatherOrSearched = new Conditions(LogicType.OR, foundLeather, leather);
		solvedAir = new VarbitRequirement(2063, 1);
		fixedBellow = new VarbitRequirement(2061, 1);

		solvedFire = new VarbitRequirement(2062, 1);

		elementalOreNearby = new ItemOnTileRequirement(elementalOre);
		earthNearby = new NpcCondition(NpcID.ELEM1_QIP_EARTH_ELEMENTAL_ROCK_VERSION);


		// First acc:
		// cut book, 2057 = 1
		// 1000000000000110 - Entered wall
		// Entered workshop
		// 2065 0->1
		// 1010110000000110 - (44038)
		// Leather found:
		// 2066 0->1
		// 1110110000000110 - (60422)
		// Turned valve 1:
		// 2059 0->1
		// 1010000000010110 - (40982)
		// Turned valve 2:
		// 2058 0->1
		// 1010000000011110 - (40990)
		// Fixed wheel:
		// 2060 0->1
		// 1010000000111110 - (41022)
		// Fixed bellow:
		// 2061 0->1
		// 1110000001111110 - (57470)
		// Repaired furnace:
		// 2062 0->1
		// 1110000011111110 - (57598)
		// Pulled air lever:
		// 2063 0->1
		// 1110001011111110 - (58110)
		// Made shield:
		// 2067 0->1
		// 100001110001011111110 - (1106686)


		// Second account, read book:
		// 2056 0->1
		// 2064 0->1->3
		// varp:
		// 0000000000000010 (2)
		// 0000010000000010 (1026)
		// 0000110000000010 (3074)
		// Cut book, 2057->1
		// 0000110000000110 (3078)
		// Entered wall
		// 1000110000000110 (35846)
	}

	@Override
	protected void setupZones()
	{
		stairwell = new Zone(new WorldPoint(2709, 3496, 0), new WorldPoint(2711, 3498, 0));
		workshop = new Zone(new WorldPoint(2682, 9862, 0), new WorldPoint(2747, 9927, 0));
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(knife, pickaxe, needle, thread, leather, hammer, coal4);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Collections.singletonList(combatGear);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Earth elemental (level 35)");
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return  Arrays.asList(
				new ExperienceReward(Skill.CRAFTING, 5000),
				new ExperienceReward(Skill.SMITHING, 5000));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("An Elemental Shield", ItemID.ELEMENTAL_SHIELD, 1));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Access to the Elemental Workshop."),
				new UnlockReward("Ability to craft and wield Elemental equipment."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Forging a Shield",
			Arrays.asList(searchBookcase, readBook, useKnifeOnBook, openOddWall, goDownStairs,
				turnEastControl, turnWestControl, pullLever, fixBellows, pullBellowsLever, getStoneBowl,
				useBowlOnLava, useLavaOnFurnace, mineRock, killRock, pickUpOre, forgeBar, smithShield),
			knife, pickaxe, needle, thread, leather, hammer, coal4));
		return allSteps;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new SkillRequirement(Skill.MINING, 20));
		req.add(new SkillRequirement(Skill.SMITHING, 20));
		req.add(new SkillRequirement(Skill.CRAFTING, 20));
		return req;
	}
}
