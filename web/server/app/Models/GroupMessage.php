<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class GroupMessage extends Model {
	protected $fillable = ['message', 'group_id'];
    protected $hidden = [];
}
?>
