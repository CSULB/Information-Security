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
        JWTAuth::parseToken()->authenticate();

        if($request->has('name') && $request->has('admin_id') && $request->has('members')) {
            $user = User::find($request->input('admin_id'));
            if(empty($user) || $user->is_verified == false) {
    			$errors = ['error' => 'Not authorized', 'code' => '0'];
    			return response()->json($errors, 500);
    		} else {
			    $group = Group::where('name', $request->input('name'))->first();
                if(!empty($group)) {
                    $errors = ['error' => 'Group name exists', 'code' => '1'];
                    return response()->json($errors, 500);
                } else {
                    $group = new Group;
                    $group->name = $request->input('name');
                    $group->admin_id = $request->input('admin_id');
                    $group->members = $request->input('members');
                    $group->save();
                    return response()->json($group);
                }
    		}
        } else {
            $errors = ['error' => 'Missing Parameters', 'code' => '0'];
            return response()->json($errors, 500);
        }
    }

    // Send message to group with ID = $id
    public function sendMessage(Request $request, $groupId) {

        JWTAuth::parseToken()->authenticate();
        $parameters = $request->all()['nameValuePairs'];

        // Sender should exist and be verified
		if($parameters['sender_id'] && $parameters['message']) {
			$sender = User::find($parameters['sender_id']);
			if(empty($sender) || $sender->is_verified == false) {
				$errors = ['error' => 'Invalid ID', 'code' => '0'];
				return response()->json($errors);
			} else {
                // Group should exist and the sender should be a member
                $group = Group::find($groupId);
                if(empty($group) || array_has((array) $group->members, $parameters['sender_id'])) {
                    $errors = ['error' => 'Invalid ID', 'code' => '1'];
                    return response()->json($errors);
                } else {
                    $message = new GroupMessage();
                    $message->group_id = $groupId;
                    $message->message = $parameters['message'];
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
        JWTAuth::parseToken()->authenticate();

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

    public function getDetails(Request $request) {
        // JWTAuth::parseToken()->authenticate();

        $parameters = $request->all()['nameValuePairs'];

        if(array_has($parameters, 'id') && array_has($parameters, 'group_id')) {
            $group = Group::find($parameters['group_id']);
            if(empty($group)) {
                return response()->json(['error' => 'Invalid ID'], 500);
            } else {
                $members = substr($group->members, 1, strlen($group->members) - 1);
                $members = explode(',', $members);
                $members = array_map('trim', $members);
                // print_r(in_array(intval($parameters['id']), $members)); exit;
                // Should be a member of the group to request the data.
                if(in_array(intval($parameters['id']), $members)) {
                    return response()->json($group);
                } else {
                    return response()->json(['error' => 'Invalid ID'], 500);
                }
            }
        }
    }
}
