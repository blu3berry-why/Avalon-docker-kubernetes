import unittest
import requests

baseurl = 'https://ktor-avalon-rest.herokuapp.com/'

users = [
    {
        "username": "testUser1",
        "password": "jelszo123"
    },
    {
        "username": "testUser2",
        "password": "jelszo123"
    },
    {
        "username": "testUser3",
        "password": "jelszo123"
    },
    {
        "username": "testUser4",
        "password": "jelszo123"
    },
    {
        "username": "testUser5",
        "password": "jelszo123"
    },
    {
        "username": "testUser6",
        "password": "jelszo123"
    },
    {
        "username": "testUser7",
        "password": "jelszo123"
    },
    {
        "username": "testUser8",
        "password": "jelszo123"
    },
    {
        "username": "testUser9",
        "password": "jelszo123"
    }, {
        "username": "testUser10",
        "password": "jelszo123"
    }, {
        "username": "testUser11",
        "password": "jelszo123"
    }
    , {
        "username": "testUser12",
        "password": "jelszo123"
    }
    , {
        "username": "testUser13",
        "password": "jelszo123"
    }
]

credentials = []

headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json'
}

yes_vote = {
    'username': 'valami',
    'uservote': 'true'
}

no_vote = {
    'username': 'valami',
    'uservote': 'false'
}


def login():
    for u in users:
        res = requests.post(baseurl + "/login",
                            json=u, headers=headers)
        credentials.append(res.json()["token"])


def create_lobby():
    my_header = {'Authorization': f'Bearer {credentials[0]}'}
    return requests.post(url=baseurl + '/createlobby', headers=my_header).json()['lobbyCode']


def start_lobby(lobby_code):
    my_header = {'Authorization': f'Bearer {credentials[0]}'}
    return requests.post(url=baseurl + '/start/' + lobby_code, headers=my_header)


login()
code = create_lobby()


class GameTests(unittest.TestCase):

    def get_lobby_info(self):
        return requests.get(url=baseurl + '/game/' + self.code, headers=self.header(0))

    def vote_yes(self, players):
        for i in players:
            requests.post(baseurl + f"/game/{self.code}/vote", headers=self.header(i), json=yes_vote)

    def vote_no(self, players):
        for i in players:
            requests.post(baseurl + f"/game/{self.code}/vote", headers=self.header(i), json=no_vote)

    def vote_adventure_yes(self, players):
        for i in players:
            requests.post(baseurl + f"/game/{self.code}/adventurevote", headers=self.header(i), json=yes_vote)

    def vote_adventure_no(self, players):
        for i in players:
            requests.post(baseurl + f"/game/{self.code}/adventurevote", headers=self.header(i), json=no_vote)

    def setUp(self):
        super().__init__()
        login()
        self.code = create_lobby()

    def test_get_basic_info(self):
        lobby_info = self.get_lobby_info().json()
        self.assertEqual(False, lobby_info['started'], msg="The just created lobby has already started!")
        self.assertEqual('NOT_DECIDED', lobby_info['winner'],
                         msg='The winner is already decided in a not started lobby')
        self.assertEqual(['UNDECIDED', 'UNDECIDED', 'UNDECIDED', 'UNDECIDED', 'UNDECIDED'], lobby_info['scores'],
                         msg='Scores are not empty in a created lobby')
        self.assertEqual(0, lobby_info['currentRound'], msg='The not started lobby should not have any other number '
                                                            'of current round than 0')
        self.assertEqual(False, lobby_info['isAdventure'],
                         msg="When creating a lobby we shouldn't be able to start an adventure")
        self.assertEqual(0, lobby_info['failCounter'], msg='Just created the lobby, fails should be 0')
        self.assertEqual([], lobby_info['selectedForAdventure'], msg='There should be no adventure to be selected for')
        self.assertEqual(False, lobby_info['assassinHasGuessed'],
                         msg="The assassin hasn't been selected yet, so they couldn't have vote")

    def test_lobby_start_too_few_players_to(self):
        for i in range(0, 4):
            self.setUp()
            print(f'starting test {i} ...')
            self.add_player_x_to_lobby(i)
            start_req = requests.post(baseurl + f"/start/{self.code}", headers=self.header(i))
            with self.subTest(i=start_req):
                self.assertEqual('Too few players', start_req.json()['message'],
                                 msg="The lobby started with too few players")
            print('end of test \n\n')

    def add_player_x_to_lobby(self, player):
        print(f'Adding player number {player} to lobby ...')
        join_req = requests.post(baseurl + f"/join/{self.code}", headers=self.header(player))

        if join_req.status_code == 200:
            print('Join successful')
        else:
            print('Join failed')

    @staticmethod
    def header(player):
        return {'Authorization': f'Bearer {credentials[player]}'}

    def test_lobby_start_ok(self):
        for i in range(4, 10):
            self.setUp()
            with self.subTest():
                for y in range(0, i + 1):
                    self.add_player_x_to_lobby(y)
                start_req = requests.post(baseurl + f"/start/{self.code}", headers=self.header(i))
                print(start_req.status_code)
                print(start_req)
                self.assertEqual(200, int(start_req.status_code), msg=f"The lobby should start with {i} players")

    # def test_lobby_start_too_many(self):
    #     for i in range(10, 12):
    #         self.setUp()
    #         with self.subTest():
    #             for y in range(0, i + 1):
    #                 self.add_player_x_to_lobby(y)
    #             start_req = requests.post(baseurl + f"/start/{self.code}", headers=self.header(i))
    #             print(start_req.status_code)
    #             print(start_req)
    #             #need fixing
    #             self.assertEqual(409, int(start_req.status_code), msg=f"The lobby shouldn't start with {i+1} players")

    def add_first_x_players_to_lobby(self, players):
        for y in range(0, players):
            self.add_player_x_to_lobby(y)

    def start_a_new_lobby(self, number_of_players):
        self.setUp()
        self.add_first_x_players_to_lobby(number_of_players)
        requests.post(baseurl + f"/start/{self.code}", headers=self.header(0))

    def test_select_players(self):
        self.start_a_new_lobby(5)
        req = self.select_players_for_adventure(0, ['testUser1', 'testUser2'])
        self.assertEqual(200, int(req.status_code))
        lobby_info = self.get_lobby_info()
        self.assertEqual(['testUser1', 'testUser2'], lobby_info.json()['selectedForAdventure'])

    def select_players_for_adventure(self, king, players):
        return requests.post(baseurl + f'/game/{self.code}/select', headers=self.header(king),
                             json=players)

    def test_vote_yes(self):
        self.start_a_new_lobby(5)
        self.select_players_for_adventure(0, ['testUser1', 'testUser2'])
        self.vote_yes([0, 1, 2, 3, 4])
        lobby_info = self.get_lobby_info().json()

        self.assertEqual(True, lobby_info["isAdventure"])
        self.assertEqual(1, lobby_info["currentAdventure"])
        self.assertEqual('testUser2', lobby_info["king"])
        self.assertEqual(0, lobby_info['failCounter'])

    def test_vote_no(self):
        self.start_a_new_lobby(5)
        self.select_players_for_adventure(0, ['testUser1', 'testUser2'])
        self.vote_no([0, 1, 2, 3, 4])
        lobby_info = self.get_lobby_info().json()
        print(lobby_info)

        self.assertEqual(False, lobby_info["isAdventure"])
        self.assertEqual(0, lobby_info["currentAdventure"])
        self.assertEqual('testUser2', lobby_info["king"])
        self.assertEqual(1, lobby_info['failCounter'])

    def test_voting_all_win_5_players(self):
        self.start_a_new_lobby(5)
        self.select_players_for_adventure(0, ['testUser1', 'testUser2'])
        self.vote_yes([0, 1, 2, 3, 4])
        self.vote_adventure_yes([0, 1])
        lobby_info = self.get_lobby_info().json()
        print(lobby_info)
        self.assertEqual(['GOOD', 'UNDECIDED', 'UNDECIDED', 'UNDECIDED', 'UNDECIDED'], lobby_info['scores'])
        self.select_players_for_adventure(1, ['testUser1', 'testUser2', 'testUser3'])
        self.vote_yes([0, 1, 2, 3, 4])
        self.vote_adventure_yes([0, 1])
        lobby_info = self.get_lobby_info().json()
        self.assertEqual(['GOOD', 'GOOD', 'UNDECIDED', 'UNDECIDED', 'UNDECIDED'], lobby_info['scores'])
        self.select_players_for_adventure(2, ['testUser1', 'testUser2'])
        self.vote_yes([0, 1, 2, 3, 4])
        self.vote_adventure_yes([0, 1])

        lobby_info = self.get_lobby_info().json()
        self.assertEqual(['GOOD', 'GOOD', 'GOOD', 'UNDECIDED', 'UNDECIDED'], lobby_info['scores'])

