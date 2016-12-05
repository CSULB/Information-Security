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

        if($request->has('name') && $request->has('user_id') && $request->has('members')) {
            $user = User::find($request->input('user_id'));
            if(empty($user) || $user->is_verified == false) {
    			$errors = ['error' => 'Not authorized', 'code' => '0'];
    			return response()->json($errors);
    		} else {
			    $group = Group::where('name', $request->input('name'))->first();
                if(!empty($group)) {
                    $errors = ['error' => 'Group name exists', 'code' => '1'];
                    return response()->json($errors);
                } else {
                    $group = new Group();
                    $group->name = $request->input('name');
                    $group->members = $request->input('members');
                    $group->save();
                    return response()->json($group);
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

        // Sender should exist and be verified
		if($request->has('sender_id') && $request->has('message')) {
			$sender = User::find($request->input('sender_id'));
			if(empty($sender) || $sender->is_verified == false) {
				$errors = ['error' => 'Invalid ID', 'code' => '0'];
				return response()->json($errors);
			} else {
                // Group should exist and the sender should be a member
                $group = Group::find($groupId);
                if(empty($group) || array_has((array) $group->members, $request->input('sender_id'))) {
                    $errors = ['error' => 'Invalid ID', 'code' => '1'];
                    return response()->json($errors);
                } else {
                    $message = new GroupMessage();
                    $message->group_id = $groupId;
                    $message->message = $request->input('message');
                    $message->save();
                    return response()->json($message);
                }
            }
        } else {
            $errors = ['error' => 'Missing Parameters', 'code' => '0'];
            return response()->json($errors);
        }
    }

    public function getMessages(Request $request, $groupId) {
        // JWTAuth::parseToken()->authenticate();

        // Sender should exist and be verified
		if($request->has('sender_id') && $request->has('timestamp')) {

			$sender = User::find($request->input('sender_id'));
			if(empty($sender) || $sender->is_verified == false) {
				$errors = ['error' => 'Invalid ID', 'code' => '0'];
				return response()->json($errors);
			} else {
                $group = Group::find($groupId);
                if(empty($group)) {
                    $errors = ['error' => 'Invalid ID', 'code' => '1'];
                    return response()->json($errors);
                } else {
                    $groupMessages = GroupMessage::where('created_at', '>', $request->input('timestamp'))
                   ->orderBy('created_at', 'asc')
                   ->get();
                   return response()->json($groupMessages);
                }
            }
        } else {
            $errors = ['error' => 'Missing Parameters', 'code' => '0'];
            return response()->json($errors);
        }
    }
}
