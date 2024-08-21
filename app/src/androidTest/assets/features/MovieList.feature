# This file is part of What Movie Next.
#
# Copyright (C) 2024 Christiaan Janssen
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

@MovieListFeature
Feature: Movie list
  CRUD operations on the movie list

  Scenario: Create entry
    Given an empty list of films
    When the user creates a new entry with the title "The Matrix"
    Then the entry "The Matrix" is visible

  Scenario: Show entry
    Given a list with an entry "A Beautiful Mind"
    When the user opens the entry "A Beautiful Mind"
    Then the card containing the information of "A Beautiful Mind" should be visible

  Scenario: The Movie list is scrollable
    Given a list with 100 entries, titled "movieN" where "N" is the index
    Then the entry "movie100" is not available
    When the user scrolls down to "movie100"
    Then the entry "movie100" is visible
