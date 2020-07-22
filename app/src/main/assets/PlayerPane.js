class PlayerPane {
	// Method used to create the divs inside the pane containing the image and the names of the players
	addPlayers() {
		// For each player
		this.players.forEach((player) => {
			// Create a div containing the name and the image
			let playerDiv = $(document.createElement("div"));

			// Set the background to the default white maze pattern
			playerDiv.css(
				"background-image",
				"url('" + images.white_maze.replace(/(\r\n|\n|\r)/gm, "") + "')"
			);

			// Add css classes
			playerDiv.addClass("player-pane-player-div");

			// Create the secret role image
			let secretRoleImg = $(document.createElement("img"));
			secretRoleImg.attr("src", images.secret_role);

			// Create the player's name tag
			let playerNameTag = $(document.createElement("p"));
			playerNameTag.addClass(`player-name-tag player_${player}`);
			playerNameTag.text(player);

			// Add both the name tag and the secret role image to the main div (playerDiv)
			playerDiv.append(secretRoleImg);
			playerDiv.append(playerNameTag);

			// Add the main div for this player to the main div of the player-pane
			this.pane.append(playerDiv);

			// Save this playerdiv in an object the key being the name
			this.playerDivs[player] = playerDiv;

			// Set an event handler for clicks that calles the playerDivClicked method passing the method the name of the player.
			playerDiv.click((e) => {
				this.playerDivClicked(player);
			});
		});
	}

	// Method used to handle clicks on the player divs. Visibility of the game-actions will be changed according to whether they
	// are selected in the player pane.

	playerDivClicked(player) {
		// Check if clicks on players are currently allowed
		// These clicks are disabled shortly after detecting the user scrolling through the player pan in order to avoid triggering unwanted clicks.
		if (this.allowPlayerClick) {
			//console.log(`Player div clicked on: ${player}`);
			//console.log(this.selected);

			// If the player is selected/ the player needs to be removed and any game-actions which involve only this player or other 'unselected' players are hidden.
			// Also, the style of the background is changed.
			if (this.selected.includes(player)) {
				// Remove the player from the array of selected players.
				this.selected.splice(this.selected.indexOf(player), 1);

				// Change the background of this player's div to the tweed pattern.
				this.playerDivs[player].css(
					"background-image",
					"url('" + images.tweed.replace(/(\r\n|\n|\r)/gm, "") + "')"
				);

				// Fetch all the game-actions (excluding those of type shuffle).
				let gameActions = $('.game-action:not(.shuffle-div)');

				// Go through all the game-actions previously fetched and check whether their classlist contains any of the classes associated with the names of selected players.
				// If not then add the class invisible making it disappear.
				for (let i = 0; i < gameActions.length; i++) {
					let classList = gameActions[i].classList;
					//console.log(classList);

					let invisible = true;

					for (let index in this.selected) {
						//console.log(`player_${this.selected[index]}`);
						if (classList.contains(`player_${this.selected[index]}`)) {
							invisible = false;
						}
					}

					if (invisible) gameActions[i].classList.add('invisible');
				}

			}

			// If the player is not selected/ the player needs to be inserted again and any game-actions which involves this player and was hidden is shown again.
			// Also, the style of the background is changed.
			else {
				// Add the player to the array of selected players
				this.selected.push(player);

				// Change the background of this player's div to the white-maze pattern
				this.playerDivs[player].css(
					"background-image",
					"url('" + images.white_maze.replace(/(\r\n|\n|\r)/gm, "") + "')"
				);

				// Fetch all the game-action elements that are currently hidden
				let invisibleGameActions = $('.game-action.invisible');

				// Check if any of the hidden game-action need to be shown again.
				for (let i = 0; i < invisibleGameActions.length; i++) {
					let classList = invisibleGameActions[i].classList;

					if (classList.contains(`player_${player}`)) {
						invisibleGameActions[i].classList.remove('invisible');
					}
				}
			}
		}
	}

	// Method to alter the offset of the playerpane in order to be able to scroll through the different elements
	// CurrentPos is the current position of the mouse/finger based on which the new offset will be calculated
	offset(currentPos) {
		console.log(`Current position: ${currentPos}`);
		console.log(`This.xPos: ${this.xPos}`);

		let offset =
			Number(this.pane.css("left").split("p")[0]) + currentPos - this.xPos;

		//console.log('Max offset: ' + this.maxOffset);

		if (offset < 0 && Math.abs(offset) <= this.maxOffset) {
			this.xPos = currentPos;
			this.pane.css("left", `${offset}px`);
		}

		console.log(`Offset: ${offset}`);
	}

	//Constructor function takes in the players as array
	constructor(players) {
		this.players = players;
		//console.log(players);
		this.selected = [];
		players.forEach((player) => this.selected.push(player));
		this.playerDivs = {};

		this.allowPlayerClick = true;
		this.pane = $(document.createElement("div"));
		this.pane.addClass("player-pane");

		this.addPlayers();

		$("#player-pane").append(this.pane);

		$(document).mouseleave((e) => {
			$(document).unbind('mousemove');
		});

		// Mouse events

		this.pane.on('mousedown touchstart', (e) => {
			this.mouseDownAt = performance.now();

			if(e.handleObj.type === 'mousedown') {
				// console.log('Mousedown: ' + e.originalEvent.x);

				this.xPos = e.originalEvent.x;

				this.pane.on('mousemove', (e) => {
					console.log('Mousemove: ' + e.originalEvent.x);

					this.offset(e.originalEvent.x);
				});

			} else {
				// console.log('Touchstart: ' + e.originalEvent.touches[0].clientX);

				this.xPos = e.originalEvent.touches[0].clientX;

				$('body').addClass('stop-scrolling');

				this.pane.on('touchmove', (e) => {
					console.log('Touchmove: ' + e.originalEvent.touches[0].clientX);

					this.offset(e.originalEvent.touches[0].clientX);
				});

			}
		});


		this.pane.on('mouseup touchend', (e) => {
			// console.log(e.handleObj.type);
			// console.log(e);

			let timeElapsed = performance.now() - this.mouseDownAt;

			if (timeElapsed > 70) {
				this.allowPlayerClick = false;

				setTimeout(() => {
					this.allowPlayerClick = true;
				}, 10);
			}

			if(e.handleObj.type === 'touchend') {
				$('body').removeClass('stop-scrolling');

				// console.log('	Unbinding touchmove');
				$(this.pane).unbind("touchmove");
			} else {
				// console.log('	Unbinding mousemove');
				$(this.pane).unbind("mousemove");
			}
		});

		//console.log(`Width of the pane: ${this.pane[0].scrollWidth}px`);
		//console.log(`Width of the viewport: ${$(window).width()}px`);

		this.maxOffset = this.pane[0].scrollWidth - $(window).width() + $('#player-pane').css('padding-left').split('p')[0] * 2;
	}
}