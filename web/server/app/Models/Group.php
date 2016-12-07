<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Group extends Model {
	protected $fillable = ['name', 'members', 'admin_id' ,'id'];
    protected $hidden = [];
}
?>
