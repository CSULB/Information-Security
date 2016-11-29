<?php
  
namespace App\Http\Controllers;

use App\Models\User;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
  
class UserController extends Controller{

	/*
	 * Source: http://www.passwordrandom.com/most-popular-passwords
	*/
	private $commonPasswords = ["password", "123456", "12345678", "1234", "qwerty", "12345", "dragon", "pussy", "baseball", "football", "letmein", "monkey", "696969", "abc123", "mustang", "michael", "shadow", "master", "jennifer", "111111", "2000", "jordan", "superman", "harley", "1234567", "fuckme", "hunter", "fuckyou", "trustno1", "ranger", "buster", "thomas", "tigger", "robert", "soccer", "fuck", "batman", "test", "pass", "killer", "hockey", "george", "charlie", "andrew", "michelle", "love", "sunshine", "jessica", "asshole", "6969", "pepper", "daniel", "access", "123456789", "654321", "joshua", "maggie", "starwars", "silver", "william", "dallas", "yankees", "123123", "ashley", "666666", "hello", "amanda", "orange", "biteme", "freedom", "computer", "sexy", "thunder", "nicole", "ginger", "heather", "hammer", "summer", "corvette", "taylor", "fucker", "austin", "1111", "merlin", "matthew", "121212", "golfer", "cheese", "princess", "martin", "chelsea", "patrick", "richard", "diamond", "yellow", "bigdog", "secret", "asdfgh", "sparky", "cowboy"];

	public function login(Request $request) {
		// $user = User::create($request->all());
		// $user->save();
		// return response()->json($user);
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
	}

	/*
	5 = Couldn't verify. User absent or wrong code.
	*/
	public function verify(Request $request) {

		$parameters = $request->all()['nameValuePairs'];

		$user = User::find($parameters['user_id']);
		if($user != null) {
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
}

?>