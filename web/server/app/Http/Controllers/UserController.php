<?php

namespace App\Http\Controllers;

use JWTAuth;

use Carbon\Carbon;
use App\Models\User;
use App\Models\Challenge;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

use GuzzleHttp\Client;

class UserController extends Controller {

	/*
	0 = No such user
	*/
	public function login(Request $request, $step) {

		if($step == 1) {
			$parameters = $request->all();

			$rules1 = [
			        'phone' => 'required|numeric|digits_between:12,12'
			    ];
			$validator1 = Validator::make($parameters, $rules1);
			if($validator1->fails()) {
				$errors = ['error' => 'Missing parameters', 'code' => '0'];
        return response()->json($errors);
			} else {
				$user = User::where('phone', $parameters['phone'])->first();
				if(empty($user)) {
					$errors = ['error' => 'Invalid Phone', 'code' => '0'];
					return response()->json($errors);
				} else {
					$challengeEntry = Challenge::firstOrCreate(['phone' => $parameters['phone']]);
					$challengeEntry->challenge = bin2hex(random_bytes(32));
					$challengeEntry->save();

					$localChallenge = hash_hmac('sha512', $user->password_hash, $challengeEntry->challenge);

					$response = ['challenge' => $localChallenge, 'salt' => $user->salt];
					return response()->json($response);
				}
			}
		} elseif ($step == 2) {
			$parameters = $request->all();

			$rules2 = [
			        'phone' => 'required',
			        'challenge_response' => 'required'
			    ];
			$validator1 = Validator::make($parameters, $rules2);

			if($validator1->fails()) {
				$errors = ['error' => 'Missing parameters', 'code' => '0'];
				return response()->json($errors);
			} else {
				$user = User::where('phone', $parameters['phone'])->first();
				$challengeEntry = Challenge::where('phone', $parameters['phone'])->first();

				// $differenceInMinutes = Carbon::now()->diffInMinutes($challengeEntry->created_at);

				// if($differenceInMinutes > 5) {
					// $challengeEntry->delete();
				// }
				if(empty($user) || empty($challengeEntry)) {
					$errors = ['error' => 'Invalid', 'code' => '0'];
					return response()->json($errors);
				} else {
					$challenge_response = $parameters['challenge_response'];

					$localChallenge = hash_hmac('sha512', $user->password_hash, $challengeEntry->challenge);

					if(strcmp($challenge_response, $localChallenge) === 0) {
						// Match! Return JWT.
						$challengeEntry->delete();
						$token = JWTAuth::fromUser($user);
						return response()->json(compact('token'));
					} else {
						$errors = ['error' => 'Invalid', 'code' => '0'];
						return response()->json($errors);
					}
				}
			}
		}
	}

	/*
     * Codes:
     * 0 = Missing fields
     * 1 = Invalid fieds
     * 2 = Not unique
     * 3 = Passwords don't match
	*/

	public function register(Request $request)	{

		$parameters = $request->all();

		$rules1 = [
		        'phone' => 'required',
		        'first_name' => 'required',
		        'last_name' => 'required',
		        'password' => 'required',
		        'confirm_password' => 'required'
		    ];
		$validator1 = Validator::make($parameters, $rules1);

		if ($validator1->fails()) {
			$errors = ['error' => 'validation_failed', 'code' => '0'];

			foreach ((array)$validator1->errors()->messages() as $key => $value) {
				$errors['fields'][] = $key;
			}
			return response()->json($errors);
		} else {

			$rules2 = [
		        'phone' => 'numeric|digits_between:12,12'
		        ];
		    $validator2 = Validator::make($parameters, $rules2);

		    if($validator2->fails()) {
		    	$errors = ['error' => 'validation_failed', 'code' => '1'];
		    	$errors['fields'][] = 'phone';
		    	return response()->json($errors);
		    } else {

		    	$rules3 = [
		    			'phone' => 'unique:users,phone'
		    		];
		    	$validator3 = Validator::make($parameters, $rules3);

		    	if($validator3->fails()) {
		    		$errors = ['error' => 'validation_failed', 'code' => '2'];
		    		$errors['fields'][] = 'phone';

		    		// Return ID for re-registering
		    		$user = User::firstOrNew(['phone' => $parameters['phone']]);
		    		$errors['id'] = $user->id;
		    		$errors['phone'] = $parameters['phone'];
		    		return response()->json($errors);
		    	} else {
		    		$rules4 = [
		    			'password' => 'min:8|regex:/^\S*(?=\S{8,})(?=\S*[a-z])(?=\S*[A-Z])(?=\S*[\d])\S*$/'
		    		];
		    		$validator4 = Validator::make($parameters, $rules4);

		    		if ($validator4->fails()) {
		    			$errors = ['error' => 'validation_failed', 'code' => '1'];
		    			$errors['fields'][] = 'password';
		    			return response()->json($errors);
		    		} else {

		    			if(strcmp($parameters['password'], $parameters['confirm_password']) != 0) {
		    				$errors = ['error' => 'validation_failed', 'code' => '3'];
		    				return response()->json($errors);
		    			} else {
							// print_r($parameters); exit;
		    				// All OKAY. Send SMS.
		    				$code = random_int(100000, 999999);
		    				if($this->sendSMS($code, $parameters['phone'])) {
		    					$user = User::firstOrNew(['phone' => $parameters['phone']]);
		    					$user->first_name = $parameters['first_name'];
		    					$user->last_name = $parameters['last_name'];
		    					$user->verification_code = $code;
		    					$user->is_verified = false;
		    					// Hash this later for more security
		    					$user->phone = $parameters['phone'];
		    					$user->salt = bin2hex(random_bytes(32));

		    					$password = $parameters['password'];
		    					$user->password_hash = hash('sha512', $user->salt.$password);
		    					$user->save();

		    					return response()->json($user);
		    				} else {
		    					$errors = ['error' => 'Couldn\'t connect to SMS server', 'code' => '4'];
		    					return response()->json($errors);
		    				}
		    			}
		    		}
		    	}
		    }
		}
	}

	public function sendSMS($code, $phone) {
		return true;
		// $formParams = [
		// 	'version' => '2.0',
		// 	'userid' => 'gauravbhor',
		// 	'vasid' => '10188',
		// 	'password' => 'Gaurav123',
		// 	'from' => '27126',
		// 	'to' => substr($phone, 2),
		// 	'text' => 'Use this code for verification in SecureChat '.$code
		// ];
		//
		// $client = new Client();
		// $request = $client->request('POST', 'http://smsapi.wire2air.com/smsadmin/submitsm.aspx', ['form_params' => $formParams]);
		//
		// if($request->getStatusCode() != 200) {
		// 	return false;
		// } else {
		// 	return true;
		// }
	}

	/*
	4 = Couldn't verify. User absent or wrong code.
	5 = User doesn't exist.
	*/
	public function verify(Request $request) {

		$parameters = $request->all()['nameValuePairs'];

		$user = User::find($parameters['user_id']);
		if(empty($user)) {
			if(strcmp($user->verification_code, $parameters['code']) == 0) {
				// Code is verified. Login the user.
				$user->verification_code = '';
				$user->is_verified = true;
				$user->save();
				return response()->json($user);
			} else {
				// Invalid verification code.
				$errors = ['error' => 'Couldn\'t verify', 'code' => '5'];
		    					return response()->json($errors);
			}
		} else {
			$errors = ['error' => 'Couldn\'t verify', 'code' => '6'];
		    					return response()->json($errors);
		}
	}

	public function getUser(Request $request, $id) {
		JWTAuth::parseToken();
		$user = JWTAuth::parseToken()->authenticate();
		$user = User::find($id);
		// echo $id; exit;
		if(empty($user)) {
			$errors = ['error' => 'Invalid', 'code' => '0'];
			return response()->json($errors);
		} else {
			return response()->json($user);
		}
	}
}

?>
