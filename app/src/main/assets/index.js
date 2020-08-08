let playerPane;

const getCompleteGameRoute = 'getGameJSON';
const getGameChangesRoute = 'getGameChangesJSON';

const getJSON = (route) => {
	const Http = new XMLHttpRequest();
	const url = `http://${location.host}/${route}`;
	Http.open("GET", url);
	Http.send();

	Http.onreadystatechange = (e) => {
	    if(Http.readyState === 4) {
			//console.log('HTTP response text: ' + Http.responseText);
			if(route === getCompleteGameRoute) {
				loadPage(JSON.parse(Http.responseText));
			} else if (route === getGameChangesRoute) {
				updatePage(JSON.parse(Http.responseText));
			}

	    }
	};
};

const generateEventElement = (event) => {
	let eventDiv;
	console.log(event);

	if (event.type === "legislative-session") {
		eventDiv = generateLegislativeSession(event);
	} else if (event.type === "executive-action") {
		eventDiv = generateExecutiveAction(event);
	} else if (event.type === "shuffle") {
		eventDiv = generateShuffle(event);
	}

	eventDiv.addClass(event.id);

	return eventDiv;
}

const updatePage = (changes) => {
	// public static String EVENT_UPDATE = "event_update";
    // public static String EVENT_DELETE = "event_delete";
	// public static String NEW_EVENT = "new_event";

	console.log(changes);

	changes.forEach((change) => {
		console.log(change);
		if (change.change_type === 'event_update') {
			$(`.${change.event.id}`).replaceWith(generateEventElement(change.event));

			if (change.event.type === 'executive-action') {
				if (change.event.executive_action_type === 'execution') {
					let oldTarget = $(`.dead_${change.event.id}`);
					console.log(oldTarget);

					if (oldTarget.length != 0) {
						console.log(oldTarget.attr('id'));
						playerPane.removeDeadFlag(oldTarget.attr('id'));
					}

					playerPane.setDeadFlag(change.event.target, change.event.id);
				} else if (change.event.executive_action_type === 'investigate_loyalty') {
					let oldTarget = $(`.inv_loyalty_${change.event.id}`);
					console.log(oldTarget);

					if (oldTarget.length != 0) {
						console.log(oldTarget.attr('id'));
						playerPane.removeInvLoyaltyFlag(oldTarget.attr('id'));
					}

					playerPane.setInvLoyaltyFlag(change.event.target, change.event.claim, change.event.id);
				}
			}

		} else if (change.change_type === 'event_delete') {
			$(`.${change.event.id}`).remove();

			if (change.event.type === 'executive-action') {
				if (change.event.executive_action_type === 'execution') {
					playerPane.removeDeadFlag(change.event.target);
				} else if (change.event.executive_action_type === 'investigate_loyalty') {
					playerPane.removeInvLoyaltyFlag(change.event.target);
				}
			}

		} else if (change.change_type === 'new_event') {
			$('#game-log').append(generateEventElement(change.event));

			if (change.event.type === 'executive-action') {
				if (change.event.executive_action_type === 'execution') {
					playerPane.setDeadFlag(change.event.target, change.event.id);
				} else if (change.event.executive_action_type === 'investigate_loyalty') {
					playerPane.setInvLoyaltyFlag(change.event.target, change.event.claim, change.event.id);
				}
			}
		} else {
			console.log('Unknown change type');
		}
	})

}


const loadPage = (gameData) => {
	console.log(gameData);

	let events = gameData.game.plays;

	playerPane = new PlayerPane(gameData.game.players);

	for (let i = 0; i < events.length; i++) {
		$("#game-log").append(generateEventElement(events[i]));

		if(events[i].type === 'executive-action') {
			if (events[i].executive_action_type === 'execution') {
				playerPane.setDeadFlag(events[i].target, events[i].id);
			} else if (events[i].executive_action_type === 'investigate_loyalty') {
				playerPane.setInvLoyaltyFlag(events[i].target, events[i].claim, events[i].id);
			}
		}
	}
};

const generateLegislativeSession = (play) => {
	// Create the main div that contains all of the legislative session html
	let lsDiv = $(document.createElement("div"));
	lsDiv.addClass("game-action legislative-session");

	//Add the player names as clases, so that they can be recognised and made invisible/visible when the user clicks on the player in the player pane
	lsDiv.addClass(`player_${play.president}`);
	lsDiv.addClass(`player_${play.chancellor}`);

	// Create the main title of the legislative session div
	let lsTitle = $(document.createElement("h1"));
	lsTitle.addClass("game-action-title");
	// The text of the legislative session title contains special text if the chancellor was rejected or the policy vetoed
	lsTitle.text(
		`Legislative Session #${play.num} ${
		play.rejected === true ? "- Rejected" : ""
		} ${play.veto === true ? "- Vetoed" : ""}`
	);
	// Append the title to the main div
	lsDiv.append(lsTitle);

	// Append the president div and the chancellor div to the main div of the legislative session
	lsDiv.append(getLeaderDiv(play, "president"));
	lsDiv.append(getLeaderDiv(play, "chancellor"));

	// Create the div containing the data on the policy played/that would have been played (in case of veto)
	if (play.rejected === false) {
		let polPlayedDiv = $(document.createElement("div"));
		polPlayedDiv.addClass("policyplayed");

		let polPlayedSpan = $(document.createElement("span"));
		polPlayedSpan.addClass("policy-played-span");
		if (play.veto === true) polPlayedSpan.addClass("cross-out");
		polPlayedSpan.text("Policy played");

		polPlayedDiv.append(polPlayedSpan);

		let polPlayedIcon = $(document.createElement("img"));
		polPlayedIcon.addClass("policy-icon");
		polPlayedIcon.attr(
			"src",
			play.policy_played === "B" ? images.bird : images.skull
		);
		polPlayedIcon.attr("alt", "policy-played-icon");

		polPlayedDiv.append(polPlayedIcon);

		lsDiv.append(polPlayedDiv);
	}

	return lsDiv;
};

const getLeaderDiv = (play, type) => {
	// Create the div containing the token for the leader and the name of the leader
	let leaderDiv = $(document.createElement("div"));
	leaderDiv.addClass(type);

	// Create the leader's token
	let leaderToken = $(document.createElement("img"));
	leaderToken.addClass("token");
	leaderToken.attr("src", images[`${type}_token`]);
	leaderToken.attr("alt", `${type}-token`);
	leaderDiv.append(leaderToken);

	// Create the leader's name span
	let leaderNameSpan = $(document.createElement("span"));
	leaderNameSpan.addClass(`${type}-name`);
	if (type === "chancellor" && play.rejected === true)
		leaderNameSpan.addClass("cross-out");
	leaderNameSpan.text(play[type]);
	leaderDiv.append(leaderNameSpan);

	// Create the leader's claim span
	if (play.rejected === false) {
		let leaderClaimSpan = $(document.createElement("span"));
		leaderClaimSpan.addClass("right-align");
		leaderClaimSpan.html(getColouredClaim(play[`${type}_claim`]));
		leaderDiv.append(leaderClaimSpan);
	}

	return leaderDiv;
};

const generateExecutiveAction = (play) => {
	let executiveActionHTML = "";

	// Get HTML code that highlights the players names using the getPlayerHTML function
	let presidentHTML = getPlayerHTML(play.president);
	let targetHTML = play.target != undefined ? getPlayerHTML(play.target) : "";

	switch (play.executive_action_type) {
		case "execution":
			executiveActionHTML = `President ${presidentHTML} selects to execute ${targetHTML}.`;
			break;
		case "investigate_loyalty":
			executiveActionHTML = `President ${presidentHTML} sees the party membership of ${targetHTML} and claims to see a member of the ${
				play.claim === "B" ? '<span class="claim-span-blue">liberal</span>' : '<span class="claim-span-red">fascist</span>'
				} team.`;
			break;
		case "policy_peek":
			executiveActionHTML = `President ${presidentHTML} peeks at the next three policies and claims to see ${getColouredClaim(
				play.claim
			)}.`;
			break;
		case "special_election":
			executiveActionHTML = `President ${presidentHTML} has chosen to special elect ${targetHTML} as president.`;
			break;
		default:
			executiveActionHTML = "Unknwon executive action type";
			break;
	}

	// Create the main div containing all the other elements
	let execActDiv = $(document.createElement("div"));
	execActDiv.addClass("game-action executive-action");

	//Add the names of the involved players as clases, so that they can be recognised and made invisible/visible when the user clicks on the player in the player pane
	execActDiv.addClass(`player_${play.president}`);

	if (targetHTML != "") execActDiv.addClass(`player_${play.target}`);

	// Create the div containing the title and the executive action icon
	let execActDivTop = $(document.createElement("div"));
	execActDivTop.addClass("executive-action-top");

	execActDiv.append(execActDivTop);

	let execActTitle = $(document.createElement("h1"));
	execActTitle.addClass("game-action-title executive-action-title");
	execActTitle.text("Executive Action");

	execActDivTop.append(execActTitle);

	let execActImg = $(document.createElement("img"));
	execActImg.addClass("executive-action-icon");
	execActImg.attr("src", images[play.executive_action_type]);
	execActImg.attr("alt", `${play.executive_action_type}-icon`);

	execActDivTop.append(execActImg);

	// Create the p element that contains the text of the executive action
	let execActText = $(document.createElement("p"));
	execActText.addClass("executive-action-text");
	execActText.html(executiveActionHTML);

	execActDiv.append(execActText);

	return execActDiv;
};

// Function that returns HTML code that highlights the name of players
const getPlayerHTML = (name) => {
	return `<span class="player-name">${name != null ? name : ""}</span>`;
};

// Function that returns HTML code that correctly colours the letters in policy claims (liberals => blue, fascists => red)
const getColouredClaim = (claim) => {
	//console.log(claim.split(''));
	let result = "";
	let chars = claim.split("");

	chars.forEach((char) => {
		result += `<span class="${
			char === "R" ? "claim-span-red" : "claim-span-blue"
			}">${char}</span>`;
	});

	return result;
};

const generateShuffle = (play) => {
	// Create the main container
	let shuffleDiv = $(document.createElement("div"));
	shuffleDiv.addClass("game-action shuffle-div");

	// Create the title of the shuffle div
	let shuffleDivTitle = $(document.createElement("h1"));
	shuffleDivTitle.addClass("game-action-title shuffle-title");
	shuffleDivTitle.text("Cards shuffled");

	shuffleDiv.append(shuffleDivTitle);

	// Create the container holding the number of policies and the image for both policy types
	let shuffleDivBody = $(document.createElement("div"));
	shuffleDivBody.addClass("shuffle-div-body");
	shuffleDiv.append(shuffleDivBody);

	shuffleDivBody.append(createPolicyCard(play, "fascist"));
	shuffleDivBody.append(createPolicyCard(play, "liberal"));

	// Append the shuffle div to the game-log section
	return shuffleDiv;
};

const createPolicyCard = (play, type) => {
	// Create the div for the policy card
	let policyDiv = $(document.createElement("div"));
	policyDiv.addClass("policy-number-div");

	// Create the image for the policy
	let policyImg = $(document.createElement("img"));
	policyImg.addClass("pol-card");
	policyImg.attr("src", images[`${type}_policy`]);
	policyImg.attr("alt", `${type}_policy_card`);

	policyDiv.append(policyImg);

	// Create the div showing the number of policies remaining in the draw pile
	let policyNum = $(document.createElement("div"));
	policyNum.addClass("policy-number");
	policyNum.text(play[`${type}_policies`]);

	policyDiv.append(policyNum);

	return policyDiv;
};

getJSON(getCompleteGameRoute);

setInterval(() => getJSON(getGameChangesRoute), 2000);