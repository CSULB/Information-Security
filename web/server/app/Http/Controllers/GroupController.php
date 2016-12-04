<?php

namespace App\Http\Controllers;

use JWTAuth;

use App\Models\GroupMessage;
use App\Models\Group;
use App\Models\User;

use Carbon\Carbon;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class GroupController extends Controller {

    public function createGroup(Request $request) {
        // JWTAuth::parseToken()->authenticate();
        if($request->has('name') && $request->has('id')) {
            $user = User::find($request->input('id'));
            if(empty($user)) {
    			$errors = ['error' => 'Not authorized', 'code' => '0'];
    			return response()->json($errors);
    		} else {
    			if($user->is_verified == true) {
                    $group = new Group();
                    $group->name = $request->input('name');
                    $group->save();
                    return response()->json($group);
    			} else {
    				$errors = ['error' => 'Not authorized', 'code' => '0'];
    				return response()->json($errors);
    			}
    		}
        } else {
            $errors = ['error' => 'Missing Parameters', 'code' => '0'];
            return response()->json($errors);
        }
    }

    // Send message to group with ID = $id
    public function sendMessage(Request $request, $groupId) {
        // JWTAuth::parseToken()->authenticate();
        $group = Group::find($groupId);
        if(empty($group)) {
            $errors = ['error' => 'Invalid ID', 'code' => '0'];
            return response()->json($errors);
        } else {
            if($request->has('message')) {
                $message = new GroupMessage();
                $message->group_id = $groupId;
                $message->message = $request->input('message');
                $message->save();
                return response()->json($message);
            } else {
                $errors = ['error' => 'Missing Parameters', 'code' => '0'];
                return response()->json($errors);
            }
        }
    }
}
