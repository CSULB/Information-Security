<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;
use App\Libraries\RestValidator;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     *
     * @return void
     */
    public function register()
    {
        //
    }

    /*
	 * Credits to https://laracasts.com/discuss/channels/general-discussion/how-to-return-error-code-of-validation-fields-in-rest-api/replies/23216
    */
    public function boot() {
        Validator::resolver(function($translator, $data, $rules, $messages) {
            return new RestValidator($translator, $data, $rules, $messages);
        });
    }
}
