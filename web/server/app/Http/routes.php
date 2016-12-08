<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

$app->get('/', function () use ($app) {
    return $app->version();
});

$app->group(['prefix' => 'api/v1'], function($app) {
    $app->post('users/login/{step}','UserController@login');
    $app->post('users/register','UserController@register');
    $app->post('users/verify','UserController@verify');

    $app->post('users/{id}', 'UserController@getUser');
    $app->post('users/message/{id}', 'UserController@sendMessage');
    $app->post('users/message/{id}/{mid}', 'UserController@getMessages');

    $app->post('groups/create', 'GroupController@createGroup');
    $app->post('groups/details', 'GroupController@getDetails');
    $app->post('groups/messages/send/{id}', 'GroupController@sendMessage');

    // Get all your messages
    $app->post('groups/message', 'GroupController@getMessages');

    $app->post('keys/dh','KeyController@diffie');
});
